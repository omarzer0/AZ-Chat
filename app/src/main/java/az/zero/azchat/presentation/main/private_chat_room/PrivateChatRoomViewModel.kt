package az.zero.azchat.presentation.main.private_chat_room

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import az.zero.azchat.common.*
import az.zero.azchat.common.event.Event
import az.zero.azchat.data.models.group.Group
import az.zero.azchat.data.models.message.Message
import az.zero.azchat.data.models.status.Status
import az.zero.azchat.repository.MainRepositoryImpl
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class PrivateChatRoomViewModel @Inject constructor(
    private val repository: MainRepositoryImpl,
    private val stateHandler: SavedStateHandle,
    private val privateRoomUseCase: PrivateRoomUseCase,
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    private val gid = stateHandler.get<String>("gid") ?: ""
    val username = stateHandler.get<String>("username") ?: ""
    val userImage = stateHandler.get<String>("userImage") ?: ""
    private val otherUserUID = stateHandler.get<String>("otherUserUID") ?: ""
    private var newGroupChat = stateHandler.get<Boolean>("isNewGroup") ?: false

    private val _messageImage = MutableLiveData<Uri?>()
    val messageImage: LiveData<Uri?> = _messageImage

    private val _event = MutableLiveData<Event<PrivateChatEvents>>()
    val event: LiveData<Event<PrivateChatEvents>> = _event

    fun getUID() = repository.getUID()


    fun getMessagesQuery(): Query {
        return repository.getMessagesQuery(gid)
    }


    fun postAction(action: PrivateChatActions) {
        when (action) {
            is PrivateChatActions.MessageLongClick -> {
                logMe("Tabbed ${action.message.messageText}")
                tryAsyncNow(viewModelScope) {
                    firestore.collection(MESSAGES_ID).document(gid)
                        .collection(PRIVATE_MESSAGES_ID)
                        .document(action.message.id!!)
                        .update("loved", !action.message.loved!!).addOnCompleteListener {
                            if (it.isSuccessful) {
                                logMe("update love done!")
                            } else {
                                logMe("update love failed! ${it.exception}")
                            }
                        }
                }
            }
            is PrivateChatActions.SendMessage -> {
                if (action.messageText.isEmpty() && messageImage.value == null) return

                if (newGroupChat) {
                    privateRoomUseCase.addGroup(
                        gid,
                        otherUserUID,
                        action.messageText,
                        messageImage.value,
                        onSuccess = { newGroupChat = it }
                    )

                } else {
                    privateRoomUseCase.checkForImageAndSend(
                        action.messageText,
                        messageImage.value,
                        gid
                    )
                }
            }
            PrivateChatActions.ViewPaused -> {
                logMe("ViewPaused")
                privateRoomUseCase.updateCurrentUserStatus(
                    gid,
                    UserStatus.OFFLINE
                )

                privateRoomUseCase.clearOtherUserStatusListener()
            }
            PrivateChatActions.ViewResumed -> {
                logMe("ViewResumed")
                privateRoomUseCase.updateCurrentUserStatus(
                    gid,
                    UserStatus.ONLINE
                )

                privateRoomUseCase.getOtherUserStatus(gid, otherUserUID, onSuccess = { state ->
                    _event.postValue(Event(PrivateChatEvents.OtherUserState(state)))
                })
            }
            is PrivateChatActions.Writing -> {
                if (action.isWriting) privateRoomUseCase.updateCurrentUserStatus(
                    gid,
                    UserStatus.WRITING
                ) else {
                    privateRoomUseCase.updateCurrentUserStatus(
                        gid,
                        UserStatus.ONLINE
                    )
                }
            }
            PrivateChatActions.DataChanged -> {
                privateRoomUseCase.setAllMessagesAsSeen(gid)
            }
        }
    }

    fun onMessageImageSelected(uri: Uri?) {
        _messageImage.value = uri
    }

}


sealed class PrivateChatActions {
    data class MessageLongClick(val message: Message) : PrivateChatActions()
    data class SendMessage(val messageText: String) : PrivateChatActions()
    data class Writing(val isWriting: Boolean) : PrivateChatActions()
    object DataChanged : PrivateChatActions()


    object ViewPaused : PrivateChatActions()
    object ViewResumed : PrivateChatActions()

}

sealed class PrivateChatEvents {
    data class OtherUserState(val otherUserStatus: UserStatus) : PrivateChatEvents()
}


class PrivateRoomUseCase @Inject constructor(
    private val sharedPreferenceManger: SharedPreferenceManger,
    private val firestore: FirebaseFirestore,
    private val application: Application,
    private val storage: FirebaseStorage
) {
    private var listener: ListenerRegistration? = null
    fun checkForImageAndSend(messageText: String, imageUri: Uri?, gid: String) {
        val realPath = imageUri?.let { RealPathUtil.getRealPath(application, it) } ?: ""
        if (realPath.isNotEmpty()) {
            val userId = sharedPreferenceManger.uid
            val timeInMill = System.currentTimeMillis()
            uploadImageByUserId(
                application.contentResolver,
                realPath,
                storage.reference.child("profileImages/$userId/$timeInMill.jpg"),
                onUploadImageSuccess = {
                    sendMessage(messageText, it.toString(), gid)
                },
                onUploadImageFailed = {
                    logMe("Failed to upload! checkForImageAndSend")
                })
        } else {
            sendMessage(messageText, "", gid)
        }

    }

    private fun sendMessage(messageText: String, imageUri: String, gid: String) {
        val randomId = firestore.collection(GROUPS_ID).document().id
        val message = Message(
            randomId,
            messageText,
            Timestamp(Date()),
            sharedPreferenceManger.uid,
            deleted = false,
            updated = false,
            loved = false,
            seen = false,
            imageUrl = imageUri
        )

        logMe("repo\n$message")
        firestore.collection(MESSAGES_ID).document(gid)
            .collection(PRIVATE_MESSAGES_ID)
            .document(randomId).set(message)

        firestore.collection(GROUPS_ID).document(gid)
            .update("lastSentMessage", message)
    }

    fun clearOtherUserStatusListener() {
        listener?.remove()
    }

    fun getOtherUserStatus(
        gid: String,
        otherUserID: String,
        onSuccess: (UserStatus) -> Unit
    ) {
        listener = firestore.collection(MESSAGES_ID).document(gid)
            .collection(USER_STATUS).document(otherUserID).addSnapshotListener { value, error ->
                if (error != null) {
                    logMe("otherUserStatus $error")
                    return@addSnapshotListener
                }
                if (value == null) return@addSnapshotListener

                val status = value.toObject<Status>() ?: return@addSnapshotListener
                val userStatus = when {
                    status.writing!! -> UserStatus.WRITING
                    status.online!! -> UserStatus.ONLINE
                    else -> UserStatus.OFFLINE
                }
                onSuccess(userStatus)
            }
    }

    fun updateCurrentUserStatus(gid: String, userStatus: UserStatus) {
        val status: Status = when (userStatus) {
            UserStatus.ONLINE -> {
                Status(writing = false, online = true)
            }
            UserStatus.WRITING -> {
                Status(writing = true, online = true)
            }
            UserStatus.OFFLINE -> {
                Status(writing = false, online = false)
            }
        }
        firestore.collection(MESSAGES_ID).document(gid)
            .collection(USER_STATUS).document(sharedPreferenceManger.uid).set(status)
    }

    fun setAllMessagesAsSeen(gid: String) {
        val uid = sharedPreferenceManger.uid
        firestore.collection(MESSAGES_ID).document(gid)
            .collection(PRIVATE_MESSAGES_ID)
            .whereEqualTo("seen", false)
            .get().addOnSuccessListener { querySnapShot ->
                querySnapShot.documents.forEach {
                    val message = it.toObject<Message>() ?: return@forEach
                    if (message.sentBy != uid) {
                        it.reference.update("seen", true)
                    }
                }
            }

        firestore.collection(GROUPS_ID).document(gid).get().addOnSuccessListener {
            val group = it.toObject<Group>() ?: return@addOnSuccessListener
            val message = group.lastSentMessage ?: return@addOnSuccessListener
            if (message.sentBy == uid)  return@addOnSuccessListener
            message.seen = true
            firestore.collection(GROUPS_ID).document(gid).update("lastSentMessage", message)
        }
    }

    fun addGroup(
        gid: String,
        otherUserID: String,
        messageText: String,
        messageImage: Uri?,
        onSuccess: (Boolean) -> Unit
    ) {
        val uID = sharedPreferenceManger.uid
        // get random id
        val randomId = abs(Random().nextLong())
        val message = Message(
            randomId.toString(),
            messageText,
            Timestamp(Date()),
            sharedPreferenceManger.uid,
            deleted = false,
            updated = false,
            loved = false,
            seen = false
        )

        checkIfGroupExists(uID, otherUserID, onSuccess = { exists ->
            logMe("exist checkIfGroupExists $exists")
            if (!exists) {
                val newGroup = Group(
                    gid,
                    "new g1",
                    false,
                    listOf(uID, otherUserID),
                    Timestamp(Date()),
                    Timestamp(Date()),
                    uID,
                    "",
                    message,
                    firestore.document("users/$uID"),
                    firestore.document("users/$otherUserID")
                )
                firestore.collection(GROUPS_ID)
                    .document(gid)
                    .set(newGroup).addOnCompleteListener {
                        onSuccess(it.isSuccessful)
                    }
            } else {
                onSuccess(true)
            }
            logMe("add check")
            checkForImageAndSend(messageText, messageImage, gid)
        })


    }

    private fun checkIfGroupExists(
        uID: String,
        otherUserID: String,
        onSuccess: (exists: Boolean) -> Unit
    ) {
        firestore.collection(GROUPS_ID)
            .whereArrayContains("members", uID)
            .get().addOnSuccessListener {
                val exists = it.any { document ->
                    val group = document.toObject<Group>()
                    if (group.hasNullField()) return@addOnSuccessListener
                    group.members!!.contains(otherUserID)
                }
                onSuccess(exists)
            }
    }
}

enum class UserStatus {
    ONLINE,
    WRITING,
    OFFLINE
}