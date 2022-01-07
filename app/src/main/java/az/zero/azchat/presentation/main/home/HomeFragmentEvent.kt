package az.zero.azchat.presentation.main.home

sealed class HomeFragmentEvent {
    class PrivateChatsClick(val gid: String) : HomeFragmentEvent()
    object AddChat : HomeFragmentEvent()
}