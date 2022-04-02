package az.zero.azchat.domain.models.group

import android.os.Parcelable
import az.zero.azchat.domain.models.message.Message
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Group(
    var gid: String? = null,
    var name: String? = null,
    var ofTypeGroup: Boolean? = null,
    var members: List<String>? = null,
    var createdAt: Timestamp? = null,
    var modifiedAt: Timestamp? = null,
    var createdBy: String? = null,
    var image: String? = "",
    var lastSentMessage: Message? = null,
    var user1: String? = "",
    var user2: String? = "",
    var groupNotificationTopic: String? = "",
    var about: String? = ""
) : Parcelable {
    init {
        if (groupNotificationTopic == null) groupNotificationTopic = ""
    }

    fun hasNullField() =
        listOf(
            gid,
            name,
            ofTypeGroup,
            members,
            createdAt,
            modifiedAt,
            createdBy,
            image,
            about
        ).any { it == null }
}
