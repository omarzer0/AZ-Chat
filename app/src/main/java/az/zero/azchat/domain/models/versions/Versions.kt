package az.zero.azchat.domain.models.versions

import android.os.Parcelable
import az.zero.azchat.domain.models.simple_info.SimpleInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class Versions(
    val header: String = "",
    val links: List<SimpleInfo> = emptyList(),
    val note: String = "",
    val version: String = "",
    val newVersionLink: String = "",
    val linksHeader: String = "",
) : Parcelable
