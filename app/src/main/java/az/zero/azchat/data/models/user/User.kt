package az.zero.azchat.data.models.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var uid: String? = null,
    var name: String? = null,
    var address: String? = null,
    var imageUrl: String? = null,
    var bio: String? = null,
    var groups: List<String>? = null
) : Parcelable {
    fun hasNullField() =
        listOf(uid, name, address, imageUrl, bio, groups).any { it == null }
}
