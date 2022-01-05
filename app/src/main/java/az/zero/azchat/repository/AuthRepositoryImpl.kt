package az.zero.azchat.repository

import android.app.Activity
import android.app.Application
import android.net.Uri
import android.util.Log
import az.zero.azchat.common.*
import az.zero.azchat.data.models.country_code.Countries
import az.zero.azchat.data.models.country_code.CountryCode
import az.zero.azchat.data.models.group.Group
import az.zero.azchat.data.models.message.Message
import az.zero.azchat.data.models.user.User
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val sharedPreferenceManger: SharedPreferenceManger,
    private val application: Application
) {

    private val TAG = "TAG"

    fun login(
        phoneNumber: String,
        activity: Activity,
        onCodeSentListener: () -> Unit,
        onVerificationSuccess: (String) -> Unit,
        onVerificationFailed: (String) -> Unit,
        onVerificationTimeOut: () -> Unit
    ) {
        // clear sharedPreferenceManger.authToken
        sharedPreferenceManger.authToken = ""
        sharedPreferenceManger.phoneNumber = phoneNumber
        logMe("login")

        val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                logMe("onVerificationCompleted: ${phoneAuthCredential.smsCode ?: "null code"}")
                signInWithPhoneAuthCredential(
                    activity,
                    phoneAuthCredential,
                    onVerificationSuccess,
                    onVerificationFailed
                )
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                logMe("onVerificationFailed: ${exception.localizedMessage ?: "null msg"}")
                val msg = exception.localizedMessage?.let {
                    if (it.contains("A network error")) "Check Internet connection!"
                    else "Number format is incorrect please enter a correct number"
                } ?: "Code not sent"

                onVerificationFailed(msg)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)
                logMe("onCodeSent: $verificationId $token")
                sharedPreferenceManger.authToken = verificationId
                onCodeSentListener()
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {
                super.onCodeAutoRetrievalTimeOut(p0)
                logMe("onCodeAutoRetrievalTimeOut")
                onVerificationTimeOut()
            }
        }
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callback)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun sendVerificationCode(
        activity: Activity,
        verificationCode: String,
        onVerificationSuccess: (String) -> Unit,
        onVerificationFailed: (String) -> Unit,
    ) {
        val credential =
            PhoneAuthProvider.getCredential(sharedPreferenceManger.authToken, verificationCode)
        signInWithPhoneAuthCredential(
            activity,
            credential,
            onVerificationSuccess,
            onVerificationFailed
        )
    }

    private fun signInWithPhoneAuthCredential(
        activity: Activity,
        credential: PhoneAuthCredential,
        onVerificationSuccess: (String) -> Unit,
        onVerificationFailed: (String) -> Unit,
    ) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Log.e(TAG, "signInWithCredential: Success")
                    val user = task.result?.user
                    Log.e(TAG, "signInWithPhoneAuthCredential: ${user?.uid}")
                    user?.let {
                        onVerificationSuccess(it.uid)
                        sharedPreferenceManger.uid = it.uid
                    }
                } else {
                    Log.e(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Log.e(TAG, "signInWithPhoneAuthCredential: Invalid code")
                    }
                    onVerificationFailed("Invalid code")
                }
            }
    }

    suspend fun getAllCountryCodes(
        onSuccess: (List<CountryCode>) -> Unit,
        onFailure: (List<CountryCode>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val gson = Gson()
            try {
                val allCountries =
                    gson.fromJson(readFile(application, "CountryCode.json"), Countries::class.java)
                onSuccess(allCountries.countries.sortedBy { it.name })

            } catch (e: Exception) {
                logMe(e.localizedMessage ?: "Unknown")
                onFailure(emptyList())
            }
        }
    }

    fun checkIfUserExists(
        onExist: () -> Unit,
        onDoesNotExist: () -> Unit,
        onFail: (String) -> Unit,
    ) {
        firestore.collection(USERS_ID).document(sharedPreferenceManger.uid).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    logMe("exist")
                    onExist()
                } else {
                    logMe("does not exist")
                    onDoesNotExist()
                }
            }
            .addOnFailureListener {
                logMe(it.localizedMessage ?: "Failed")
                onFail(it.localizedMessage ?: "Failed")
            }
    }

    fun addUser(
        user: User,
        onAddUserSuccess: () -> Unit,
        onAddUserFail: (String) -> Unit
    ) {
        val uid = sharedPreferenceManger.uid
        user.uid = uid
        user.phoneNumber = sharedPreferenceManger.phoneNumber
        firestore.collection(USERS_ID).document(uid).set(user)
            .addOnSuccessListener {
                onAddUserSuccess()
            }.addOnFailureListener {
                onAddUserFail(it.localizedMessage ?: "Failed")
                logMe(it.localizedMessage ?: "addUser error")
            }


    }

    fun uploadProfileImageByUserId(
        uri: Uri,
        onUploadImageSuccess: (Uri) -> Unit,
        onUploadImageFailed: (String) -> Unit,
    ) {
        val realPath = RealPathUtil.getRealPath(application, uri)
//        val type = realPath.split(".")[1]
        val file = Uri.fromFile(File(realPath))
        val userId = sharedPreferenceManger.uid
        val storageRef = storage.reference.child("profileImages/$userId.jpg")
        val uploadTask = storageRef.putFile(file)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    logMe(it.localizedMessage ?: "taskUrl unknown error")
                    onUploadImageFailed(it.localizedMessage ?: "unknown error")
                }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                logMe("success $downloadUri")
                onUploadImageSuccess(downloadUri)
            } else {
                logMe("taskUrl addOnCompleteListener failed")
                onUploadImageFailed("unknown error")
            }
        }
    }

    //________________________________________________________________________________


    fun getAllGroupsByUserUID() {
        firestore.collection(GROUPS_ID).whereArrayContains("members", sharedPreferenceManger.uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document: DocumentSnapshot in documents) {
                    val group = document.toObject<Group>() ?: continue
                    if (!group.hasNullField()) {
                        Log.e(TAG, "$group")
                    } else {
                        Log.e(TAG, "HAS NULL : => $group")
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, it.localizedMessage ?: "Unknown")
            }
    }

    fun getMessagesByGroupId(gid: String) {
        firestore.collection(MESSAGES_ID)
            .document(gid)
            .collection(PRIVATE_MESSAGES_ID)
            .orderBy("sentAt")
            .get()
            .addOnSuccessListener { docs ->
                docs.forEach { doc ->
                    Log.e(TAG, "msg: ${doc.data}")
                }
            }
            .addOnFailureListener {
                Log.e(TAG, "getMessagesByGroupId: ${it.localizedMessage}")
            }
    }

    fun addGroup() {
        // get random id
        val gid = firestore.collection(GROUPS_ID).document().id
        Log.e(TAG, "addGroup: $gid")

        val newGroup = Group(
            gid,
            "new g1",
            false,
            emptyList(),
            Timestamp(Date()),
            Timestamp(Date()),
            TEST_USER

        )
        firestore.collection(GROUPS_ID)
            .document(gid)
            .set(newGroup)
    }

    fun addMessage(message: Message, gid: String) {
        firestore.collection(MESSAGES_ID).document(gid).collection(PRIVATE_MESSAGES_ID)
            .document().set(message)
    }


}