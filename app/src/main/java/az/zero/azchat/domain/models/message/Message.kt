package az.zero.azchat.domain.models.message

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    var id: String? = null,
    var messageText: String? = null,
    var sentAt: Timestamp? = null,
    var sentBy: String? = null,
    var deleted: Boolean? = null,
    var updated: Boolean? = null,
    var loved: Boolean? = null,
    var seen: Boolean = false,
    var imageUrl:String = ""
) : Parcelable {
    fun hasNullField() =
        listOf(id, messageText, sentAt, sentBy, deleted, updated, loved).any { it == null }

    var clicked: Boolean = false
}