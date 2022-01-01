package az.zero.azchat.data.models.message

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    var messageText: String? = null,
    var sentAt: Timestamp? = null,
    var sentBy: String? = null
) : Parcelable {
    fun hasNullField() =
        listOf(messageText, sentAt, sentBy).any { it == null }
}
