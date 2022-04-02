package az.zero.azchat.presentation.main.chat_details

import android.net.Uri

open class ChatDetailsEvent {
    data class UploadImageSuccess(val downloadUri: Uri) : ChatDetailsEvent()
    data class UploadImageFailed(val error: String) : ChatDetailsEvent()
    data class UpdateText(val isName: Boolean, val value: String) : ChatDetailsEvent()
    object UploadingImageLoading : ChatDetailsEvent()
}