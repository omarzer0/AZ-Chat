package az.zero.azchat.domain.models.simple_info

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class SimpleInfo(
    val icon: String = "",
    val link: String = "",
    val name: String = "",
    val type: Int = -1,
) : Parcelable


enum class InfoTypes(value: Int) {
    ELSE(-1),
    BROWSER(0),
    FACEBOOK(1),
    EMAIL(2),
    PHONE(3),
    WHATS(4);

    companion object {
        fun getByValue(value: Int) = values().firstOrNull { it.ordinal-1 == value } ?: ELSE
    }
}


