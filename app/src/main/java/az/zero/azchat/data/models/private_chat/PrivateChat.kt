package az.zero.azchat.data.models.private_chat

import az.zero.azchat.data.models.group.Group
import az.zero.azchat.data.models.user.User

data class PrivateChat(
    val group: Group,
    val user: User
)
