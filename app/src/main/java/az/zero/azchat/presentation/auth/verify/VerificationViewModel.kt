package az.zero.azchat.presentation.auth.verify

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import az.zero.azchat.common.event.Event
import az.zero.azchat.repository.AuthRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val repository: AuthRepositoryImpl
) : ViewModel() {

    private val _event = MutableLiveData<Event<VerificationEvent>>()
    val event: LiveData<Event<VerificationEvent>> = _event


    fun sendVerificationCode(activity: Activity, verificationCode: String) {
        _event.postValue(Event(VerificationEvent.VerifyButtonClick))
        repository.sendVerificationCode(activity, verificationCode,
            onVerificationSuccess = {
                _event.postValue(Event(VerificationEvent.VerificationSuccess(it)))
            },
            onVerificationFailed = {
                _event.postValue(Event(VerificationEvent.VerificationFailed(it)))
            })
    }

    fun getIfUserExist() {
        repository.checkIfUserExists(
            onExist = {
                _event.postValue(Event(VerificationEvent.UserExist))
            },
            onDoesNotExist = {
                _event.postValue(Event(VerificationEvent.UserDoesNotExist))
            },
            onFail = {
                _event.postValue(Event(VerificationEvent.OnUserExistCallFail(it)))
            }
        )
    }
}

