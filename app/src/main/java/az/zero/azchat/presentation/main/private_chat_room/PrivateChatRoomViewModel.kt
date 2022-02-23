package az.zero.azchat.presentation.main.private_chat_room

import android.net.Uri
import androidx.lifecycle.*
import az.zero.azchat.common.*
import az.zero.azchat.common.event.Event
import az.zero.azchat.domain.models.message.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PrivateChatRoomViewModel @Inject constructor(
    private val stateHandler: SavedStateHandle,
    private val sendMessageHelper: SendMessageHelper,
    private val firestore: FirebaseFirestore,
    private val sharedPreferenceManger: SharedPreferenceManger
) : ViewModel() {

    private val gid = stateHandler.get<String>("gid") ?: ""
    val username = stateHandler.get<String>("username") ?: ""
    val userImage = stateHandler.get<String>("userImage") ?: ""
    private val notificationToken = stateHandler.get<String>("notificationToken") ?: ""
    private val otherUserUID = stateHandler.get<String>("otherUserUID") ?: ""
    private var newGroupChat = stateHandler.get<Boolean>("isNewGroup") ?: false

    private val _messageImage = MutableLiveData<Uri?>()
    val messageImage: LiveData<Uri?> = _messageImage

    private val _event = MutableLiveData<Event<PrivateChatEvents>>()
    val event: LiveData<Event<PrivateChatEvents>> = _event

    fun getUID() = sharedPreferenceManger.uid


    fun getMessagesQuery(): Query {
        return firestore.collection(MESSAGES_ID)
            .document(gid)
            .collection(PRIVATE_MESSAGES_ID)
            .orderBy("sentAt", Query.Direction.ASCENDING)
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
                    sendMessageHelper.addGroup(
                        gid,
                        otherUserUID,
                        action.messageText,
                        messageImage.value,
                        onSuccess = { newGroupChat = it },
                        notificationToken = notificationToken
                    )

                } else {
                    sendMessageHelper.checkForImageAndSend(
                        action.messageText,
                        messageImage.value,
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

enum class UserStatus {
    ONLINE,
    WRITING,
    OFFLINE
}