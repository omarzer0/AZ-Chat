package az.zero.azchat.presentation.auth.verify

open class VerificationEvent {
    data class VerificationSuccess(val uid: String) : VerificationEvent()
    data class VerificationFailed(val msg: String?) : VerificationEvent()
    object VerifyButtonClick : VerificationEvent()
}