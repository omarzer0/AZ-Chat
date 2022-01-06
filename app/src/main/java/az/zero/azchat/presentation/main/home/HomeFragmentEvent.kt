package az.zero.azchat.presentation.main.home

import az.zero.azchat.data.models.private_chat.PrivateChat

sealed class HomeFragmentEvent {
    class GetPrivateChats(val privateChats: List<PrivateChat>) : HomeFragmentEvent()
    class PrivateChatsClick(val gid: String) : HomeFragmentEvent()
    object AddChat : HomeFragmentEvent()
}