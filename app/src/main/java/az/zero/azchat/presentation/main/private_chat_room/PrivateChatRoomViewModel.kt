package az.zero.azchat.presentation.main.private_chat_room

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
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
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class PrivateChatRoomViewModel @Inject constructor(
    private val repository: MainRepositoryImpl,
    private val stateHandler: SavedStateHandle,
    private val privateRoomUseCase: PrivateRoomUseCase
) : ViewModel() {

    private val gid = stateHandler.get<String>("gid") ?: ""
    val username = stateHandler.get<String>("username") ?: ""
    val userImage = stateHandler.get<String>("userImage") ?: ""
    private val otherUserUID = stateHandler.get<String>("otherUserUID") ?: ""
    private var newGroupChat = stateHandler.get<Boolean>("isNewGroup") ?: false

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
            }
            is PrivateChatActions.SendMessage -> {
                if (newGroupChat) {
                    privateRoomUseCase.addGroup(gid, otherUserUID, action.messageText,
                        onSuccess = { newGroupChat = it }
                    )

                } else {
                    privateRoomUseCase.sendMessage(action.messageText, gid)
                }
            }
            PrivateChatActions.ViewPaused -> {
                privateRoomUseCase.updateCurrentUserStatus(
                    gid,
                    UserStatus.OFFLINE
                )

                privateRoomUseCase.clearOtherUserStatusListener()
            }
            PrivateChatActions.ViewResumed -> {
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

//    init {
//        privateRoomUseCase.setAllMessagesAsSeen(gid)
//    }

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
    object MessageLongClicked : PrivateChatEvents()
    data class OtherUserState(val otherUserStatus: UserStatus) : PrivateChatEvents()
}


class PrivateRoomUseCase @Inject constructor(
    private val sharedPreferenceManger: SharedPreferenceManger,
    private val firestore: FirebaseFirestore,
) {
    private var listener: ListenerRegistration? = null
    fun sendMessage(messageText: String, gid: String) {
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

        logMe("repo\n$message")
        firestore.collection(MESSAGES_ID).document(gid)
            .collection(PRIVATE_MESSAGES_ID)
            .document().set(message)

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
    }

    fun addGroup(
        gid: String,
        otherUserID: String,
        messageText: String,
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
            sendMessage(messageText, gid)
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