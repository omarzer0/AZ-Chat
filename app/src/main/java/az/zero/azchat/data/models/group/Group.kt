package az.zero.azchat.data.models.group

import android.os.Parcelable
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
) : Parcelable {
    fun hasNullField() =
        listOf(gid, name, ofTypeGroup, members, createdAt, modifiedAt, createdBy).any { it == null }

}
