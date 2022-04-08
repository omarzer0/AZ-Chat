package az.zero.azchat.domain.models.group

import android.os.Parcelable
import az.zero.azchat.domain.models.message.Message
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Group(
    var gid: String? = "",
    var name: String? = "",
    var ofTypeGroup: Boolean? = false,
    var members: List<String>? = emptyList(),
    var createdAt: Timestamp? = Timestamp(Date()),
    var modifiedAt: Timestamp? = Timestamp(Date()),
    var createdBy: String? = "",
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


    // TODO:hasNullField refactor it
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
