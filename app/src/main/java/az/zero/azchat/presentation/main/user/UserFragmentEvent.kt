package az.zero.azchat.presentation.main.user

import android.net.Uri

open class UserFragmentEvent {
    data class UploadImageSuccess(val downloadUri: Uri) : UserFragmentEvent()
    data class UploadImageFailed(val error: String) : UserFragmentEvent()
    data class UpdateText(val isName: Boolean, val value: String) : UserFragmentEvent()
    object UploadingImageLoading : UserFragmentEvent()
}