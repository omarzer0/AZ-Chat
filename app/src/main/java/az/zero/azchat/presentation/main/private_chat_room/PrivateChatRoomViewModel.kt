package az.zero.azchat.presentation.main.private_chat_room

import android.net.Uri
import androidx.lifecycle.*
import az.zero.azchat.common.*
import az.zero.azchat.common.event.Event
import az.zero.azchat.di.remote.ApplicationScope
import az.zero.azchat.domain.models.group.Group
import az.zero.azchat.domain.models.message.Message
import az.zero.azchat.domain.models.private_chat.PrivateChat
import az.zero.azchat.presentation.main.adapter.messages.MessageLongClickAction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PrivateChatRoomViewModel @Inject constructor(
    private val stateHandler: SavedStateHandle,
    private val sendMessageHelper: SendMessageHelper,
    private val firestore: FirebaseFirestore,
    private val sharedPreferenceManger: SharedPreferenceManger,
    private val storage: FirebaseStorage,
    private val firebaseMessaging: FirebaseMessaging,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    private val privateChat = stateHandler.get<PrivateChat>("privateChat")!!

    //    private val gid = stateHandler.get<String>("gid") ?: ""
    //    val username = stateHandler.get<String>("username") ?: ""
    //    val userImage = stateHandler.get<String>("userImage") ?: ""
    //    private val notificationToken = stateHandler.get<String>("notificationToken") ?: ""
    //    private val otherUserUID = stateHandler.get<String>("otherUserUID") ?: ""
    //    private var newGroupChat = stateHandler.get<Boolean>("isNewGroup") ?: false

    val isGroup = privateChat.group.ofTypeGroup ?: false
    private val gid = privateChat.id

    val username = privateChat.user.name ?: ""
    val userImage = privateChat.user.imageUrl ?: ""

    val groupName = privateChat.group.name ?: ""
    val groupImage = privateChat.group.image ?: ""

    private val privateChatNotificationToken = privateChat.user.notificationToken ?: ""
    private val groupNotificationTopic = privateChat.group.groupNotificationTopic ?: ""

    private val otherUserUID = if (isGroup) "" else privateChat.user.uid ?: ""
    private var newGroupChat = stateHandler.get<Boolean>("isNewGroup") ?: false


    private val valueMap = HashMap<String, Any>()

    private var messageToEdit: Message? = null
    private val _editAreaState = MutableLiveData<Pair<Boolean, String>>()
    val editAreaState: LiveData<Pair<Boolean, String>> = _editAreaState

    private var messageImage: Uri? = null
    private var messageAudio: Uri? = null

    private val _event = MutableLiveData<Event<PrivateChatEvents>>()
    val event: LiveData<Event<PrivateChatEvents>> = _event

    fun getUID() = sharedPreferenceManger.uid

    fun getGID() = gid


    fun getMessagesQuery(): Query {
        return firestore.collection(MESSAGES_ID)
            .document(gid)
            .collection(PRIVATE_MESSAGES_ID)
            .orderBy("sentAt", Query.Direction.ASCENDING)
    }

    fun postAction(action: PrivateChatActions) {
        when (action) {
            is PrivateChatActions.ReceiverMessageLongClick -> {
                logMe("Tabbed ${action.message.messageText}")
                val message = action.message
                updateMessageField(message.id!!, getValueMap().apply {
                    set("loved", !message.loved!!)
                })
            }
            is PrivateChatActions.SendMessage -> {
                if (action.messageText.isEmpty() && messageImage == null && messageAudio == null) return

                val notificationToken = if (isGroup) groupNotificationTopic
                else privateChatNotificationToken
                if (newGroupChat) {
                    sendMessageHelper.addGroup(
                        gid,
                        otherUserUID,
                        action.messageText,
                        messageImage,
                        messageAudio,
                        action.messageType,
                        onSuccess = { newGroupChat = it },
                        notificationToken = notificationToken,
                        isGroup = isGroup,
                        groupName = groupName,
                        groupImage = groupImage
                    )

                } else {
                    sendMessageHelper.checkForImageOrAudioAndSend(
                        action.messageType,
                        action.messageText,
                        messageImage,
                        messageAudio,
                        gid,
                        notificationToken,
                        isGroup = isGroup,
                        groupName = groupName,
                        groupImage = groupImage
                    )
                }
            }
            PrivateChatActions.ViewPaused -> {
                if (isGroup) return
                logMe("ViewPaused")
                sendMessageHelper.updateCurrentUserStatus(
                    gid,
                    UserStatus.OFFLINE
                )

                sendMessageHelper.clearOtherUserStatusListener()
            }
            PrivateChatActions.ViewResumed -> {
                if (isGroup) return
                logMe("ViewResumed")
                sendMessageHelper.updateCurrentUserStatus(
                    gid,
                    UserStatus.ONLINE
                )

                sendMessageHelper.getOtherUserStatus(gid, otherUserUID, onSuccess = { state ->
                    _event.postValue(Event(PrivateChatEvents.OtherUserState(state)))
                })
            }
            is PrivateChatActions.Writing -> {
                if (isGroup) return
                if (action.isWriting) sendMessageHelper.updateCurrentUserStatus(
                    gid,
                    UserStatus.WRITING
                ) else {
                    sendMessageHelper.updateCurrentUserStatus(
                        gid,
                        UserStatus.ONLINE
                    )
                }
            }
            PrivateChatActions.DataChanged -> {
                sendMessageHelper.setAllMessagesAsSeen(gid)
            }
            is PrivateChatActions.SenderMessageLongClick -> {
                handleMessageMenuClick(action.message, action.clickAction)
            }
            PrivateChatActions.CancelEditClick -> {
                _editAreaState.value = Pair(false, "")
                messageToEdit = null
            }
            is PrivateChatActions.SendEditedMessage -> {
                if (messageToEdit == null) return
                if (messageToEdit!!.messageText == action.text) return

                val text = action.text
                updateMessageField(messageToEdit!!.id!!, getValueMap().apply {
                    set("updated", true)
                    set("messageText", text)
                })

                updateHomeMessage(messageToEdit!!, getValueMap().apply {
                    set("lastSentMessage", messageToEdit!!.copy(updated = true))
                    set(
                        "lastSentMessage",
                        messageToEdit!!.copy(messageText = text)
                    )
                })
            }
        }
    }

    fun onMessageImageSelected(uri: Uri?) {
        messageImage = uri
        postAction(PrivateChatActions.SendMessage("", MessageType.IMAGE))
    }

    fun uploadAudioFile(mLocalFilePath: String, duration: Long) {
        val currentTimeMillis = System.currentTimeMillis()
        val audioFilePath = "${currentTimeMillis}.3gp"
        val ref = storage.reference.child("audio/${getUID()}/${audioFilePath}")
        val localUri = Uri.fromFile(File(mLocalFilePath))
        ref.putFile(localUri).addOnSuccessListener {
            logMe("uploadAudioFile: success")
            getDownloadableUrl(audioFilePath, duration)
        }.addOnFailureListener {
            logMe("uploadAudioFile: ${it.localizedMessage ?: "Unknown"}")
        }
    }

    private fun getDownloadableUrl(audioFilePath: String, audioDuration: Long) {
        val notificationToken = if (isGroup) groupNotificationTopic
        else privateChatNotificationToken
        storage.reference.child("audio/${getUID()}/${audioFilePath}").downloadUrl.addOnSuccessListener {
            sendMessageHelper.checkForImageOrAudioAndSend(
                MessageType.AUDIO,
                "",
                null,
                it,
                gid,
                notificationToken,
                audioDuration,
                isGroup = isGroup,
                groupName = groupName,
                groupImage = groupImage
            )
        }
    }

    private fun handleMessageMenuClick(message: Message, clickAction: MessageLongClickAction) {
        when (clickAction) {
            MessageLongClickAction.EDIT -> {
                _editAreaState.value = Pair(true, message.messageText!!)
                messageToEdit = message
            }
            MessageLongClickAction.DELETE -> {
                updateMessageField(message.id!!, getValueMap().apply {
                    set("deleted", true)
                })

                updateHomeMessage(message, getValueMap().apply {
                    set("lastSentMessage", message.copy(updated = true))
                })
            }
        }
    }

    private fun getValueMap() = valueMap.apply {
        clear()
    }

    private fun updateMessageField(
        documentId: String,
        hashMap: HashMap<String, Any>,
        onSuccess: (() -> Unit)? = null,
        onFail: ((String) -> Unit)? = null,
        onFinish: (() -> Unit)? = null
    ) {
        tryAsyncNow(viewModelScope, action = {
            firestore.collection(MESSAGES_ID).document(gid)
                .collection(PRIVATE_MESSAGES_ID)
                .document(documentId)
                .update(hashMap).await()
            onSuccess?.invoke()
        }, error = {
            onFail?.invoke(it.localizedMessage ?: "updateMessageField unknown error")
        }, finally = {
            onFinish?.invoke()
        })
    }

    private fun updateHomeMessage(message: Message, hashMap: HashMap<String, Any>) {
        tryAsyncNow(tag = "updateDeleteHome", scope = applicationScope, action = {
            val path = firestore.collection(GROUPS_ID).document(gid).get().await()
            val group = path.toObject<Group>() ?: return@tryAsyncNow
            if (group.lastSentMessage!!.id != message.id!!) return@tryAsyncNow
            firestore.collection(GROUPS_ID).document(gid)
                .update(hashMap).await()
            logMe("updateDeleteHome: Success", "updateDelete")
        }, error = {
            logMe("updateDeleteHome: failed ${it.localizedMessage}", "updateDelete")
        })
    }

    fun isEditMode(): Boolean = _editAreaState.value?.first ?: false

    init {
        if (isGroup) firebaseMessaging.subscribeToTopic(groupNotificationTopic)
    }
}

sealed class PrivateChatActions {
    data class ReceiverMessageLongClick(val message: Message) : PrivateChatActions()
    data class SendMessage(val messageText: String, val messageType: MessageType) :
        PrivateChatActions()

    data class SenderMessageLongClick(
        val message: Message,
        val clickAction: MessageLongClickAction
    ) : PrivateChatActions()

    object CancelEditClick : PrivateChatActions()

    data class Writing(val isWriting: Boolean) : PrivateChatActions()
    data class SendEditedMessage(val text: String) : PrivateChatActions()

    object DataChanged : PrivateChatActions()


    object ViewPaused : PrivateChatActions()
    object ViewResumed : PrivateChatActions()

}

sealed class PrivateChatEvents {
    data class OtherUserState(val otherUserStatus: UserStatus) : PrivateChatEvents()
}

enum class UserStatus {
    ONLINE,
    WRITING,
    OFFLINE
}

enum class MessageType {
    TEXT,
    AUDIO,
    IMAGE
}