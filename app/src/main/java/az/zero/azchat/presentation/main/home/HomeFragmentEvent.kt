package az.zero.azchat.presentation.main.home

import az.zero.azchat.domain.models.private_chat.PrivateChat

sealed class HomeFragmentEvent {
    data class PrivateChatsClick(
        val privateChat: PrivateChat
    ) : HomeFragmentEvent()

    object AddChat : HomeFragmentEvent()
}