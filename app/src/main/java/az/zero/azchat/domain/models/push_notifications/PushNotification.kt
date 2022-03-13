package az.zero.azchat.domain.models.push_notifications

data class PushNotification(
    val data: NotificationData,
    val to: String
)

data class NotificationData(
    val title: String,
    val message: String,
    val hasImage: Boolean,
    val hasVoice: Boolean,

    val gid: String,
    val username: String,
    val userImage: String,
    val notificationToken: String,
    val otherUserUID: String,
    val isGroup: Boolean,

    val groupName: String,
    val groupImage: String
)