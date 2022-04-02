package az.zero.azchat.domain.models.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var uid: String? = null,
    var name: String? = null,
    var imageUrl: String? = "",
    var bio: String? = null,
    var groups: List<String>? = null,
    var phoneNumber: String? = null,
    var notificationToken: String? = "",
    var blockList :List<String> = emptyList()
) : Parcelable {
    fun hasNullField(): Boolean {
        if (notificationToken == null) notificationToken = ""
        return listOf(
            uid,
            name,
            imageUrl,
            bio,
            groups,
            phoneNumber,
            notificationToken
        ).any { it == null }
    }
}
