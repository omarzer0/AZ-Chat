package az.zero.azchat.presentation.main.home

sealed class HomeFragmentEvent {
    class PrivateChatsClick(val gid: String, val username: String,val userImage:String) : HomeFragmentEvent()
    object AddChat : HomeFragmentEvent()
}