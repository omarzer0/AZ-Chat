package az.zero.azchat.domain.models.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var uid: String? = "",
    var name: String? = "",
    var imageUrl: String? = "",
    var bio: String? = "",
    var groups: List<String>? = emptyList(),
    var phoneNumber: String? = "",
    var notificationToken: String? = "",
    var blockList: List<String> = emptyList(),
    var numberIsHidden: Boolean = true
) : Parcelable {

    // TODO:hasNullField refactor this
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
