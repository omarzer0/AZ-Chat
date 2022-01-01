package az.zero.phoneloginmvvm.presentation.verify

open class VerifyState {
    data class VerificationSuccess(val uid: String) : VerifyState()
    data class VerificationFailed(val msg: String?) : VerifyState()
}