package az.zero.phoneloginmvvm.data.models.user

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String,
    val name: String,
    val address: String,
    val imageUrl: String,
    val bio: String,
    val groups: List<String>
) : Parcelable
