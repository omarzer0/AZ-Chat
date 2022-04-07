package az.zero.azchat.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import az.zero.azchat.common.*
import az.zero.azchat.domain.models.user.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repositoryImpl: NotificationTokenHelper,
    private val firestore: FirebaseFirestore,
    private val sharedPreferenceManger: SharedPreferenceManger
) : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private fun getUserInfo() {
        val uid = sharedPreferenceManger.uid
        firestore.collection(USERS_ID).document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                logMe("getUserInfo $error")
                return@addSnapshotListener
            }

            if (value == null) return@addSnapshotListener
            val listenerUser = value.toObject<User>() ?: return@addSnapshotListener
            _user.postValue(listenerUser)
        }
    }

    private fun getNotificationToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { newToken ->
            logMe("newToken= $newToken")
            repositoryImpl.updateUserToken(newToken)
        }
    }

    fun logOut() {
        tryNow { firestore.clearPersistence() }
    }

    fun getUser(): User? = user.value

    init {
        tryNow {
            getUserInfo()
            getNotificationToken()
        }
    }
}

