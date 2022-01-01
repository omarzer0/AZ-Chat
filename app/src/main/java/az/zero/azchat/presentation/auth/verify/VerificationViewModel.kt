package az.zero.azchat.presentation.auth.verify

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import az.zero.azchat.common.Event
import az.zero.azchat.repository.AuthRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val repository: AuthRepositoryImpl
) : ViewModel() {

    private val _state = MediatorLiveData<Event<VerifyState>>()
    val state: LiveData<Event<VerifyState>> = _state

    fun sendVerificationCode(activity: Activity, verificationCode: String) {
        repository.sendVerificationCode(activity, verificationCode,
            onVerificationSuccess = {
                _state.value = Event(VerifyState.VerificationSuccess(it))
            },
            onVerificationFailed = {
                _state.value = Event(VerifyState.VerificationFailed(it))
            })
    }
}

