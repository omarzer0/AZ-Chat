package az.zero.azchat.presentation.main.private_chat_room

import android.net.Uri
import androidx.lifecycle.*
import az.zero.azchat.common.*
import az.zero.azchat.common.event.Event
import az.zero.azchat.domain.models.message.Message
import az.zero.azchat.presentation.main.adapter.messages.MessageLongClickAction
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PrivateChatRoomViewModel @Inject constructor(
    private val stateHandler: SavedStateHandle,
    private val sendMessageHelper: SendMessageHelper,
    private val firestore: FirebaseFirestore,
    private val sharedPreferenceManger: SharedPreferenceManger,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val gid = stateHandler.get<String>("gid") ?: ""
    val username = stateHandler.get<String>("username") ?: ""
    val userImage = stateHandler.get<String>("userImage") ?: ""
    private val notificationToken = stateHandler.get<String>("notificationToken") ?: ""
    private val otherUserUID = stateHandler.get<String>("otherUserUID") ?: ""
    private var newGroupChat = stateHandler.get<Boolean>("isNewGroup") ?: false


    private var messageImage: Uri? = null
    private var messageAudio: Uri? = null
//    val messageImage: LiveData<Uri?> = _messageImage

    private val _event = MutableLiveData<Event<PrivateChatEvents>>()
    val event: LiveData<Event<PrivateChatEvents>> = _event

    fun getUID() = sharedPreferenceManger.uid


    fun getMessagesQuery(): Query {
        return firestore.collection(MESSAGES_ID)
            .document(gid)
            .collection(PRIVATE_MESSAGES_ID)
            .orderBy("sentAt", Query.Direction.ASCENDING)
    }

    private fun updateMessageField(
        documentId: String,
        field: String,
        value: Any,
        onSuccess: (() -> Unit)? = null,
        onFail: ((String) -> Unit)? = null,
        onFinish: (() -> Unit)? = null
    ) {
        tryAsyncNow(viewModelScope, action = {
            firestore.collection(MESSAGES_ID).document(gid)
                .collection(PRIVATE_MESSAGES_ID)
                .document(documentId)
                .update(field, value).await()

            onSuccess?.invoke()
        }, error = {
            onFail?.invoke(it.localizedMessage ?: "updateMessageField unknown error")
        }, finally = {
            onFinish?.invoke()
        })
    }


    fun postAction(action: PrivateChatActions) {
        when (action) {
            is PrivateChatActions.ReceiverMessageLongClick -> {
                logMe("Tabbed ${action.message.messageText}")
                val message = action.message
                updateMessageField(message.id!!, "loved", !message.loved!!)
            }
            is PrivateChatActions.SendMessage -> {
                if (action.messageText.isEmpty() && messageImage == null && messageAudio == null) return

                if (newGroupChat) {
                    sendMessageHelper.addGroup(
                        gid,
                        otherUserUID,
                        action.messageText,
                        messageImage,
                        messageAudio,
                        action.messageType,
                        onSuccess = { newGroupChat = it },
                        notificationToken = notificationToken
                    )

                } else {
                    sendMessageHelper.checkForImageOrAudioAndSend(
                        action.messageType,
                        action.messageText,
                        messageImage,
                        messageAudio,
                        gid,
                        notificationToken
                    )
                }
            }
            PrivateChatActions.ViewPaused -> {
                logMe("ViewPaused")
                sendMessageHelper.updateCurrentUserStatus(
                    gid,
                    UserStatus.OFFLINE
                )

                sendMessageHelper.clearOtherUserStatusListener()
            }
            PrivateChatActions.ViewResumed -> {
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
        storage.reference.child("audio/${getUID()}/${audioFilePath}").downloadUrl.addOnSuccessListener {
            sendMessageHelper.checkForImageOrAudioAndSend(
                MessageType.AUDIO,
                "",
                null,
                it,
                gid,
                notificationToken,
                audioDuration
            )
        }
    }

    private fun handleMessageMenuClick(message: Message, clickAction: MessageLongClickAction) {
        when (clickAction) {
            MessageLongClickAction.EDIT -> {

            }
            MessageLongClickAction.DELETE -> {
                _event.postValue(Event(PrivateChatEvents.ShowDeleteDialog))
                var success = false
                updateHomeMessage(message)
                updateMessageField(message.id!!, "deleted", true, onFail = {
                    success = false
                }, onSuccess = {
                    success = true
                }, onFinish = {
                    _event.postValue(Event(PrivateChatEvents.HideDeleteDialog(success)))
                })
            }
        }
    }

    private fun updateHomeMessage(message: Message) {
        tryAsyncNow(viewModelScope) {
            firestore.collection(GROUPS_ID).document(gid)
                .update("lastSentMessage", message.copy(deleted = true)).await()
        }
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


    data class Writing(val isWriting: Boolean) : PrivateChatActions()
    object DataChanged : PrivateChatActions()


    object ViewPaused : PrivateChatActions()
    object ViewResumed : PrivateChatActions()

}

sealed class PrivateChatEvents {
    data class OtherUserState(val otherUserStatus: UserStatus) : PrivateChatEvents()
    object ShowDeleteDialog : PrivateChatEvents()
    data class HideDeleteDialog(val success: Boolean) : PrivateChatEvents()
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