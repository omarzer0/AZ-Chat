package az.zero.azchat.presentation.login

sealed class LoginState {
    object CodeSent : LoginState()
    data class VerificationSuccess(val uid: String) : LoginState()
    data class VerificationFailed(val msg: String?) : LoginState()
}