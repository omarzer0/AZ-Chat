package az.zero.azchat.repository

import android.app.Application
import android.util.Log
import az.zero.azchat.common.*
import az.zero.azchat.data.models.group.Group
import az.zero.azchat.data.models.message.Message
import az.zero.azchat.data.models.user.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val sharedPreferenceManger: SharedPreferenceManger,
    private val application: Application
) {
    private val TAG = "tag"

    fun getAllUsers(
        onGetUsersDone: (List<User>) -> Unit,
    ) {
//        val p = storage.reference.child("profileImages/${sharedPreferenceManger.uid}.jpg")
//        p.downloadUrl.addOnSuccessListener {
//            logMe(it.toString(),"testtt")
//        }

        val uid = sharedPreferenceManger.uid
        logMe("uid ===> $uid")
        val users = ArrayList<User>()
        firestore.collection(USERS_ID).get().addOnSuccessListener { documents ->
            documents.forEach { document ->
                val user = document.toObject<User>()
                if (!user.hasNullField() && user.uid != uid) {
                    logMe("${user.uid} != $uid")
                    users.add(user)
                } else {
                    Log.e(TAG, "HAS NULL : => $user")
                }
            }
            onGetUsersDone(users)

        }.addOnFailureListener {
            logMe("getAllUsersByPhoneNumber ${it.localizedMessage}")
            onGetUsersDone(emptyList())
        }
    }

    fun getAllGroupsByUserUID() {
        logMe("getAllGroupsByUserUID---------")
        val uid = sharedPreferenceManger.uid
        firestore.collection(GROUPS_ID).whereArrayContains("members", uid)
            .get()
            .addOnSuccessListener { documents ->
                documents.forEach { document ->
                    val group = document.toObject<Group>()
                    if (group.hasNullField()) return@forEach
//                    if (!group.ofTypeGroup!!) {
//                        val otherUID = group.members!!.filter { it != uid }[0]
//                        getImageByUID(otherUID, onSuccess = {
//                            group.image = it
//                        })
//                    }
                    Log.e(TAG, "$group")
                }
            }
            .addOnFailureListener {
                Log.e(TAG, it.localizedMessage ?: "Unknown")
            }
    }

    private fun getImageByUID(uid: String, onSuccess: (String) -> Unit) {
        val imageRef = storage.reference.child("profileImages/$uid.jpg")
        imageRef.downloadUrl.addOnSuccessListener {
            logMe(it.toString())
            onSuccess(it.toString())
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
            TEST_USER,
            "",
            emptyMap(),
            Message()
        )
        firestore.collection(GROUPS_ID)
            .document(gid)
            .set(newGroup)
    }

    fun addMessage(message: Message, gid: String) {
        firestore.collection(MESSAGES_ID).document(gid).collection(PRIVATE_MESSAGES_ID)
            .document().set(message)
    }

    fun getCollectionReference(): CollectionReference {
        return firestore.collection(GROUPS_ID)
    }

}