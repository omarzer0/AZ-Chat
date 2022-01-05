package az.zero.azchat.presentation.auth.login

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.zero.azchat.common.Event
import az.zero.azchat.common.State
import az.zero.azchat.common.logMe
import az.zero.azchat.data.models.country_code.CountryCode
import az.zero.azchat.repository.AuthRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepositoryImpl
) : ViewModel() {
    private val _event = MutableLiveData<Event<LoginEvent>>()
    val event: LiveData<Event<LoginEvent>> = _event

    private val _countryCode = MutableLiveData<List<CountryCode>>()
    private val _countryCodeFromVerifyFragment = MutableLiveData<CountryCode>()

    init {
        getAllCountryCodes()
//        repository.createUserIfNotExist()
    }

    fun login(code: String, number: String, activity: Activity) {
        logMe(code)
        if (!verifyIfInputsOk(code, number)) return
        _event.postValue(Event(LoginEvent.LoginBtnClick))
        repository.login("+$code$number", activity,
            onCodeSentListener = {
                _event.postValue(Event(LoginEvent.CodeSent))
                Event(State.Success())
            },
            onVerificationSuccess = {
                _event.postValue(Event(LoginEvent.VerificationSuccess(it)))
            },
            onVerificationFailed = {
                _event.postValue(Event(LoginEvent.VerificationFailed(it)))
            })
    }

    private fun verifyIfInputsOk(code: String, number: String): Boolean {
        return if (number.isEmpty()) {
            _event.postValue(Event(LoginEvent.InvalidInputs("Please enter valid number")))
            false
        } else if (code.isEmpty()) {
            _event.postValue(Event(LoginEvent.InvalidInputs("Please select valid code")))
            false
        } else if (_countryCode.value == null && _countryCodeFromVerifyFragment.value == null) {
            _event.postValue(Event(LoginEvent.CountryCodeNull))
            false
        } else if (_countryCodeFromVerifyFragment.value != null) {
            true
        } else if (code.isEmpty() || !_countryCode.value!!.any { it.callingCode == code }) {
            _event.postValue(Event(LoginEvent.InvalidInputs("Please select valid code")))
            false
        } else true
    }


    private fun getAllCountryCodes() {
        viewModelScope.launch {
            repository.getAllCountryCodes(onSuccess = {
                _countryCode.postValue(it)
            }, onFailure = {
                _countryCode.postValue(it)
            })
        }
    }

    fun getCountryCodeByCode(code: String) {
        if (_countryCode.value == null) {
            _event.postValue(Event(LoginEvent.CountryCodeNull))
            return
        }

        val countryCode = _countryCode.value!!.find { it.callingCode == code }
        if (countryCode == null) {
            _event.postValue(Event(LoginEvent.CountryCodeInvalid))
        } else {
            _event.postValue(Event(LoginEvent.CountryCodeExists(countryCode.name)))
        }

    }

    fun getFragmentResult(countryCode: CountryCode?) {
        countryCode?.let {
            _event.postValue(
                Event(
                    LoginEvent.ReceivedCountryCodeFormOtherFragment(
                        it.callingCode,
                        it.name
                    )
                )
            )
            _countryCodeFromVerifyFragment.postValue(it)

        } ?: logMe("fragment result for some code is null")
    }

}

