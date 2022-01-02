package az.zero.azchat.presentation.auth.login

sealed class LoginEvent {
    object CodeSent : LoginEvent()
    data class ReceivedCountryCodeFormOtherFragment(
        val callingCode: String,
        val countryName: String
    ) : LoginEvent()

    data class VerificationSuccess(val uid: String) : LoginEvent()
    data class VerificationFailed(val msg: String?) : LoginEvent()
    data class InvalidInputs(val msg: String) : LoginEvent()
    data class CountryCodeExists(val countryName: String) : LoginEvent()
    object CountryCodeInvalid : LoginEvent()
    object CountryCodeNull : LoginEvent()
    object LoginBtnClick : LoginEvent()
}