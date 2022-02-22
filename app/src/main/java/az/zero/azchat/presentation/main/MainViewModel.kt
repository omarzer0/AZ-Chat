package az.zero.azchat.presentation.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.zero.azchat.domain.models.user.User
import az.zero.azchat.repository.MainRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repositoryImpl: MainRepositoryImpl,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private fun getUserInfo() {
        viewModelScope.launch {
            repositoryImpl.getUserInfo { _user.postValue(it) }
        }
    }

    private fun getNotificationToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { newToken ->
            Log.e("TAG", "onCreate: $newToken")
            repositoryImpl.updateUserToken(newToken)
        }
    }

    fun logOut() {
        firestore.clearPersistence()
    }

    init {
        getUserInfo()
        getNotificationToken()
    }
}

