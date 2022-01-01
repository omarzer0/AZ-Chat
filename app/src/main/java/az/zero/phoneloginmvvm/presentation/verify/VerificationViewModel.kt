package az.zero.phoneloginmvvm.presentation.verify

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import az.zero.phoneloginmvvm.common.Event
import az.zero.phoneloginmvvm.repository.MainRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val repository: MainRepositoryImpl
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

