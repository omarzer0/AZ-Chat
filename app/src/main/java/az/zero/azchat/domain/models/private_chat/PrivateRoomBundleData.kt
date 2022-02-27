package az.zero.azchat.domain.models.private_chat

data class PrivateRoomBundleData(
    val gid: String,
    val username: String,
    val userImage: String,
    val notificationToken: String,
    val otherUserUID: String
)