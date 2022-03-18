package az.zero.azchat.presentation.main.chat_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import az.zero.azchat.common.logMe
import az.zero.azchat.domain.models.private_chat.PrivateChat
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatDetailsViewModel @Inject constructor(
    private val stateHandler: SavedStateHandle
) : ViewModel() {

    private val privateChat = stateHandler.get<PrivateChat>("privateChat")!!
    private val group = privateChat.group
    private val user = privateChat.user
    private val isGroup = group.ofTypeGroup!!
    private val name = if (isGroup) group.name!! else user.name!!
    private val image = if (isGroup) group.image!! else user.imageUrl!!
    private val about = if (isGroup) group.about!! else user.bio!!

    fun getCurrentPrivateChat() = privateChat

    init {
        logMe("$privateChat","ChatDetailsViewModel")
    }
}

