package az.zero.phoneloginmvvm.data.models.message

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    val messageText: String,
    val sentAt: Timestamp,
    val sentBy: String
) : Parcelable
