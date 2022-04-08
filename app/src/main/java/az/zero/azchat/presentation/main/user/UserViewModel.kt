package az.zero.azchat.presentation.main.user

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.*
import az.zero.azchat.common.SharedPreferenceManger
import az.zero.azchat.common.USERS_ID
import az.zero.azchat.common.event.Event
import az.zero.azchat.common.tryAsyncNow
import az.zero.azchat.domain.models.user.User
import az.zero.azchat.presentation.auth.extra_details.UploadImageUseCase
import az.zero.azchat.presentation.main.user.UserFragment.Companion.USER_BIO_CODE_KEY
import az.zero.azchat.presentation.main.user.UserFragment.Companion.USER_NAME_CODE_KEY
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val stateHandle: SavedStateHandle,
    private val uploadImageUseCase: UploadImageUseCase,
    private val sharedPreferenceManger: SharedPreferenceManger,
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private var user = stateHandle.get<User>("user")

    private val _event = MutableLiveData<Event<UserFragmentEvent>>()
    val event: LiveData<Event<UserFragmentEvent>> = _event

    private val _imageMLD = MutableLiveData<Uri>()
    val imageMLD: LiveData<Uri> = _imageMLD

    private val _blockedUsersMLD = MutableLiveData<List<User>>()
    val blockedUsersMLD: LiveData<List<User>> = _blockedUsersMLD


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
                user = user?.copy(imageUrl = it.toString())
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

    fun getCurrentUser() = user

    fun removeUserFromBlockedList(id: String) {
        val uid = sharedPreferenceManger.uid
        firestore.collection(USERS_ID).document(uid).update("blockList", FieldValue.arrayRemove(id))
        getBlockedListUserId()
    }

    private fun getBlockedListUserId() {
        val uid = sharedPreferenceManger.uid
        tryAsyncNow(viewModelScope) {
            val user = firestore.collection(USERS_ID).document(uid)
                .get().await().toObject<User>() ?: return@tryAsyncNow
            if (user.hasNullField()) return@tryAsyncNow
            sharedPreferenceManger.blockList = user.blockList
            getBlockedUsersByUserId(user.blockList)
        }
    }

    private fun getBlockedUsersByUserId(blockList: List<String>) {
        val users = ArrayList<User>()
        tryAsyncNow(viewModelScope) {
            blockList.forEach { id ->
                val user = firestore.collection(USERS_ID).document(id).get()
                    .await().toObject<User>() ?: return@forEach
                if (!user.hasNullField()) users.add(user)
            }
            _blockedUsersMLD.postValue(users)
        }
    }

    init {
        getBlockedListUserId()
    }

}

