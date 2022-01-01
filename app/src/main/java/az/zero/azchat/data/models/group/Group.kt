package az.zero.azchat.data.models.group

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Group(
    val gid: String? = null,
    val name: String? = null,
    val ofTypeGroup: Boolean? = null,
    val members: List<String>? = null,
    val createdAt: Timestamp? = null,
    val modifiedAt: Timestamp? = null,
    val createdBy: String? = null,
) : Parcelable {
    fun hasNullField() =
        listOf(gid, name, ofTypeGroup, members, createdAt, modifiedAt, createdBy).any { it == null }

}
