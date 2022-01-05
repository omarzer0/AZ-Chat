package az.zero.azchat.presentation.main.add_chat

import az.zero.azchat.data.models.user.User

sealed class AddChatEvent {
    class GetUsersToChatDone(val users: List<User>) : AddChatEvent()
//    class GetUsersToChatFailed(val error: String) : AddChatEvent()
}