package az.zero.azchat.data.models.private_chat

import az.zero.azchat.data.models.group.Group
import az.zero.azchat.data.models.user.User
import java.util.*
import kotlin.math.abs

data class PrivateChat(
    val group: Group,
    val user: User,
//    val id: Long = abs(Random().nextLong())
    val id: String = group.gid!!
)
