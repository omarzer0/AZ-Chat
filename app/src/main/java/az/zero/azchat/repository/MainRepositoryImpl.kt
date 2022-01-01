package az.zero.azchat.repository

import android.app.Activity
import android.util.Log
import az.zero.azchat.common.*
import az.zero.azchat.data.models.group.Group
import az.zero.azchat.data.models.message.Message
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val sharedPreferenceManger: SharedPreferenceManger
) {

    private val TAG = "TAG"

    fun login(
        phoneNumber: String,
        activity: Activity,
        onCodeSentListener: () -> Unit,
        onVerificationSuccess: (String) -> Unit,
        onVerificationFailed: (String) -> Unit,
    ) {
        // clear sharedPreferenceManger.authToken
        sharedPreferenceManger.authToken = ""

        val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                Log.e(TAG, "onVerificationCompleted: ${phoneAuthCredential.smsCode ?: "null code"}")
                signInWithPhoneAuthCredential(
                    activity,
                    phoneAuthCredential,
                    onVerificationSuccess,
                    onVerificationFailed
                )
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                Log.e(TAG, "onVerificationFailed: ${exception.localizedMessage ?: "null msg"}")
                val msg = exception.localizedMessage?.let {
                    if (it.contains("A network error")) "Check Internet connection!"
                    else "Code not sent"
                } ?: "Code not sent"

                onVerificationFailed(msg)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)
                Log.e(TAG, "onCodeSent: $verificationId $token")
                sharedPreferenceManger.authToken = verificationId
                onCodeSentListener()
            }
        }
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
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

    //________________________________________________________________________________


    fun getAllGroupsByUserUID(uid: String) {
        firestore.collection(GROUPS_ID).whereArrayContains("members", uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document: DocumentSnapshot in documents) {
                    val group = document.toObject<Group>() ?: continue
                    if (!group.hasNullField()) {
                        Log.e(TAG, "$group")
                    } else {
                        Log.e(TAG, "HAS NULL : => $group")
                    }


//                    Log.e(TAG, "${document.id} => ${document.data}")
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