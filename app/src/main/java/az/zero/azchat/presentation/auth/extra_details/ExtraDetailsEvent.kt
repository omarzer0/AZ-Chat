package az.zero.azchat.presentation.auth.extra_details

import android.net.Uri

open class ExtraDetailsEvent {
    data class UploadImageSuccess(val downloadUri: Uri) : ExtraDetailsEvent()
    data class UploadImageFailed(val error: String) : ExtraDetailsEvent()
    data class ValidateUserInputsError(val error: Int) : ExtraDetailsEvent()
    data class AddUserError(val error: String) : ExtraDetailsEvent()
    object AddUserSuccess : ExtraDetailsEvent()
    object UploadingImageLoading:ExtraDetailsEvent()
    object AddUserLoading:ExtraDetailsEvent()
}