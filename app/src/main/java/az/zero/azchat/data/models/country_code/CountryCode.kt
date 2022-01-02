package az.zero.azchat.data.models.country_code

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CountryCode(
    var name: String,
    var code: String,
    var callingCode: String
) : Parcelable