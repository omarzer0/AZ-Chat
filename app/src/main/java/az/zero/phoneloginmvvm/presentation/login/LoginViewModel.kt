package az.zero.phoneloginmvvm.presentation.login

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import az.zero.phoneloginmvvm.common.Event
import az.zero.phoneloginmvvm.repository.MainRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: MainRepositoryImpl
) : ViewModel() {
    private val _state = MediatorLiveData<Event<LoginState>>()
    val state: LiveData<Event<LoginState>> = _state

    fun login(phoneNumber: String, activity: Activity) {
        repository.login(phoneNumber, activity,
            onCodeSentListener = {
                _state.value = Event(LoginState.CodeSent)
            },
            onVerificationSuccess = {
                _state.value = Event(LoginState.VerificationSuccess(it))
            },
            onVerificationFailed = {
                _state.value = Event(LoginState.VerificationFailed(it))
            })
    }
}

