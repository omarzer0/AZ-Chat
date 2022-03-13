package az.zero.azchat.domain.models.private_chat

import android.os.Parcelable
import az.zero.azchat.domain.models.group.Group
import az.zero.azchat.domain.models.user.User
import kotlinx.parcelize.Parcelize

@Parcelize
data class PrivateChat(
    val group: Group,
    val user: User,
//    val id: Long = abs(Random().nextLong())
    val id: String
) : Parcelable
