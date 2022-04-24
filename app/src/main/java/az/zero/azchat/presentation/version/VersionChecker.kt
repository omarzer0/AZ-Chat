package az.zero.azchat.presentation.version

import android.app.Application
import android.content.Intent
import az.zero.azchat.common.*
import az.zero.azchat.di.remote.ApplicationScope
import az.zero.azchat.domain.models.versions.Versions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class VersionChecker @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val application: Application,
    @ApplicationScope private val scope: CoroutineScope
) {

    operator fun invoke() {
//        val document = firestore.collection("versions").document("123321")
//        document.get().addOnSuccessListener {
//            tryNow {
//                logMe(
//                    "online version ${it["version"]} ${it["version"]?.javaClass?.name}" +
//                            "\ncurrent version $APP_VERSION_CODE ${APP_VERSION_CODE.javaClass.name}",
//                    "VersionChecker"
//                )
//                val version = it["version"] as String
//                if (version != APP_VERSION_CODE) {
//                    val intent = Intent(application, VersionActivity::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                    application.startActivity(intent)
//                }
//            }
//        }

        tryAsyncNow(scope) {
            val versions = firestore.collection(VERSIONS_ID).document(VERSIONS_DOC_ID).get().await()
                .toObject<Versions>() ?: return@tryAsyncNow

            if (versions.version != APP_CURRENT_VERSION_CODE) {
                val intent = Intent(application, VersionActivity::class.java)
                intent.putExtra(VERSIONS_EXTRA_KEY, versions)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                application.startActivity(intent)
            }
        }
    }
}