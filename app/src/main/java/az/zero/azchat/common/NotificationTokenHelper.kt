package az.zero.azchat.common

import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationTokenHelper @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val sharedPreferenceManger: SharedPreferenceManger,
) {

    fun updateUserToken(newToken: String) {
        logMe("main updateUserToken", "updateUserToken")
        val uid = sharedPreferenceManger.uid
        sharedPreferenceManger.notificationToken = newToken
        firestore.collection(USERS_ID).document(uid).update("notificationToken", newToken)
    }

}