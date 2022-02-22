package az.zero.azchat.domain.models.push_notifications

data class PushNotification(
    val data: NotificationData,
    val to: String
)

data class NotificationData(
    val title: String,
    val message: String,
    val hasImage: Boolean
)