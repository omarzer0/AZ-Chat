package az.zero.azchat.presentation.auth.extra_details

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import az.zero.azchat.R
import az.zero.azchat.common.event.Event
import az.zero.azchat.data.models.user.User
import az.zero.azchat.repository.AuthRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExtraDetailsViewModel @Inject constructor(
    private val repository: AuthRepositoryImpl
) : ViewModel() {

    private val _event = MutableLiveData<Event<ExtraDetailsEvent>>()
    val event: LiveData<Event<ExtraDetailsEvent>> = _event

    private val _imageMLD = MutableLiveData<Uri>()
    val imageMLD: LiveData<Uri> = _imageMLD

    fun uploadProfileImageByUserId(uri: Uri, contentResolver: ContentResolver) {
        _event.postValue(Event(ExtraDetailsEvent.UploadingImageLoading))
        repository.uploadProfileImageByUserId(
            uri,
            contentResolver,
            onUploadImageSuccess = {
                _imageMLD.postValue(it)
                _event.postValue(Event(ExtraDetailsEvent.UploadImageSuccess(it)))
            },
            onUploadImageFailed = {
                _event.postValue(Event(ExtraDetailsEvent.UploadImageFailed(it)))
            })
    }

    fun addUser(username: String, bio: String) {
        when {
            username.isEmpty() -> {
                _event.postValue(Event(ExtraDetailsEvent.ValidateUserInputsError(R.string.enter_user_name)))
            }
            bio.isEmpty() -> {
                _event.postValue(Event(ExtraDetailsEvent.ValidateUserInputsError(R.string.enter_bio)))
            }
            bio.length < 3 -> {
                _event.postValue(Event(ExtraDetailsEvent.ValidateUserInputsError(R.string.bio_under_three)))
            }
            else -> {
                _event.postValue(Event(ExtraDetailsEvent.AddUserLoading))
                var image = ""
                if (_imageMLD.value != null) {
                    image = _imageMLD.value.toString()
                }
                val user = User("", username, image, bio, emptyList(), "")
                repository.addUser(
                    user,
                    onAddUserSuccess = {
                        _event.postValue(Event(ExtraDetailsEvent.AddUserSuccess))
                    },
                    onAddUserFail = {
                        _event.postValue(Event(ExtraDetailsEvent.AddUserError(it)))
                    }
                )
            }
        }
    }
}

