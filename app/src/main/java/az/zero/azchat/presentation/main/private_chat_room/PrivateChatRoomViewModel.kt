package az.zero.azchat.presentation.main.private_chat_room

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import az.zero.azchat.common.logMe
import az.zero.azchat.data.models.message.Message
import az.zero.azchat.repository.MainRepositoryImpl
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PrivateChatRoomViewModel @Inject constructor(
    private val repository: MainRepositoryImpl,
    private val stateHandler: SavedStateHandle
) : ViewModel() {

    private val gid = stateHandler.get<String>("gid")

    fun getUID() = repository.getUID()


    fun getMessagesQuery(): Query {
        return repository.getMessagesQuery(gid ?: "")
    }


    fun postAction(action: PrivateChatActions) {
        when (action) {
            is PrivateChatActions.MessageLongClick -> {
                logMe("Tabbed ${action.message.messageText}")
            }
            is PrivateChatActions.SendMessage -> {
                if (gid == null) return
                logMe("send vm ${action.messageText}")
                repository.sendMessage(action.messageText, gid)
            }
        }
    }
}

sealed class PrivateChatActions {
    data class MessageLongClick(val message: Message) : PrivateChatActions()
    data class SendMessage(val messageText: String) : PrivateChatActions()

}

sealed class PrivateChatEvents {
    object MessageLongClicked : PrivateChatEvents()
}