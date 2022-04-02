package az.zero.azchat.presentation.main.user

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import az.zero.azchat.common.SharedPreferenceManger
import az.zero.azchat.common.USERS_ID
import az.zero.azchat.common.event.Event
import az.zero.azchat.domain.models.user.User
import az.zero.azchat.presentation.auth.extra_details.UploadImageUseCase
import az.zero.azchat.presentation.main.chat_details.ChatDetailsEvent
import az.zero.azchat.presentation.main.user.UserFragment.Companion.USER_BIO_CODE_KEY
import az.zero.azchat.presentation.main.user.UserFragment.Companion.USER_NAME_CODE_KEY
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val stateHandle: SavedStateHandle,
    private val uploadImageUseCase: UploadImageUseCase,
    private val sharedPreferenceManger: SharedPreferenceManger,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    val user = stateHandle.get<User>("user")

    private val _event = MutableLiveData<Event<UserFragmentEvent>>()
    val event: LiveData<Event<UserFragmentEvent>> = _event

    private val _imageMLD = MutableLiveData<Uri>()
    val imageMLD: LiveData<Uri> = _imageMLD

    fun updateUserImage(imageUrl: Uri) {
        val uid = sharedPreferenceManger.uid
        _event.postValue(Event(UserFragmentEvent.UploadingImageLoading))
        uploadImageUseCase(
            imageUrl,
            "profileImages/${uid}.jpg",
            onUploadImageSuccess = {
                _imageMLD.value = it
                _event.postValue(Event(UserFragmentEvent.UploadImageSuccess(it)))
                firestore.collection(USERS_ID).document(uid).update("imageUrl", it.toString())
            },
            onUploadImageFailed = {
                _event.postValue(Event(UserFragmentEvent.UploadImageFailed(it)))
            })
    }

    fun updateNameOrBio(bundle: Bundle) {
        if (bundle[USER_NAME_CODE_KEY] == null && bundle[USER_BIO_CODE_KEY] == null) return
        val isName = bundle[USER_NAME_CODE_KEY] != null
        val fieldToUpdate = if (isName) "name" else "bio"

        val value: String =
            if (isName) bundle[USER_NAME_CODE_KEY].toString() else bundle[USER_BIO_CODE_KEY].toString()

        val uid = sharedPreferenceManger.uid
        firestore.collection(USERS_ID).document(uid).update(fieldToUpdate, value)
        _event.postValue(Event(UserFragmentEvent.UpdateText(isName, value)))
    }

}

