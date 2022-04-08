package az.zero.azchat.presentation.version

import android.app.Application
import android.content.Intent
import az.zero.azchat.common.APP_VERSION_CODE
import az.zero.azchat.common.logMe
import az.zero.azchat.common.tryNow
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class VersionChecker @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val application: Application
) {

    operator fun invoke() {
        val document = firestore.collection("versions").document("dgLRpMhPNQxMuYVR6EVm")
        document.get().addOnSuccessListener {
            tryNow {
                logMe(
                    "online version ${it["version"]} ${it["version"]?.javaClass?.name}\ncurrent version $APP_VERSION_CODE ${APP_VERSION_CODE.javaClass.name}",
                    "VersionChecker"
                )
                val version = it["version"] as String
                if (version != APP_VERSION_CODE) {
                    val intent = Intent(application, VersionActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    application.startActivity(intent)
                }
            }
        }
    }
}