package az.zero.azchat.repository

import android.app.Application
import android.util.Log
import az.zero.azchat.common.*
import az.zero.azchat.data.models.group.Group
import az.zero.azchat.data.models.message.Message
import az.zero.azchat.data.models.private_chat.PrivateChat
import az.zero.azchat.data.models.user.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

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

//    fun getAllGroupsByUserUID() {
//        logMe("getAllGroupsByUserUID---------")
//        val uid = sharedPreferenceManger.uid
//        firestore.collection(GROUPS_ID).whereArrayContains("members", uid)
//            .get()
//            .addOnSuccessListener { documents ->
//                documents.forEach { document ->
//                    val group = document.toObject<Group>()
//                    if (group.hasNullField()) return@forEach
//                    Log.e(TAG, "$group")
//                    firestore.document(group.user1!!.path).get().addOnSuccessListener {
//                        if (it.exists()) {
//                            val user = it.toObject<User>()
//                            if (user != null && user.uid != uid) {
//                                logMe("not null or same id")
//                                group.image = user.imageUrl
//                                group.name = user.name
//                            }
//
//                        }
//                    }
//                }
//            }
//            .addOnFailureListener {
//                Log.e(TAG, it.localizedMessage ?: "Unknown")
//            }
//    }

    @ExperimentalCoroutinesApi
    fun getPrivateChatsForUser(): Flow<List<PrivateChat>> = callbackFlow {
        val uid = sharedPreferenceManger.uid
        logMe("$uid +++++++++++++")
        val listener = firestore.collection(GROUPS_ID)
            .whereArrayContains("members", uid).addSnapshotListener { value, error ->
                if (error != null) {
                    logMe("listenForGroupChanges $error")
                    return@addSnapshotListener
                }

                value?.forEach { document ->
                    val group = document.toObject<Group>()
                    if (group.ofTypeGroup == true) return@forEach
                    if (group.hasNullField()) return@forEach
                    val otherUserId =
                        if (!group.user1!!.path.contains(uid)) group.user1!!.path
                        else group.user2!!.path

                    logMe(
                        "$uid =======\n1==${group.user1!!.path}\n2==${group.user2!!.path}",
                        "userTesttttt"
                    )
                    firestore.document(otherUserId).get().addOnSuccessListener {
                        if (it.exists()) {
                            val user = it.toObject<User>() ?: return@addOnSuccessListener
                            trySend(listOf(PrivateChat(group, user)))
                        }
                    }
                }
            }

        awaitClose {
            // This block is executed when producer channel is cancelled
            // This function resumes with a cancellation exception.
            // Dispose listener
            logMe("closed", "close")
            listener.remove()
            cancel()
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

    fun updateGroupLastMessage() {
        val uid = sharedPreferenceManger.uid
        val groupId = "F0rLNHDqXZdZz6sqIeJj"
        firestore.collection(GROUPS_ID).document(groupId)
            .update(
                "lastSentMessage", Message(
                    "123123123",
                    "new msg sent",
                    Timestamp(Date()),
                    "Uhp7yfvA8HW4HIS49sQBoe7wovQ2",
                    deleted = false,
                    updated = false,
                    loved = false
                )
            )
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
        val randomId = abs(Random().nextLong())
        val textGroupID = "F0rLNHDqXZdZz6sqIeJj"
        val textOmarUserID = "9704maSB3ETKq1jF0rTtOhaUq8m2"
        val testMessageText = "Fine Thx!"

        val testMsg = Message(
            randomId.toString(),
            testMessageText,
            Timestamp(Date()),
            textOmarUserID,
            deleted = false,
            updated = false,
            loved = false
        )
        firestore.collection(MESSAGES_ID).document(textGroupID)
            .collection(PRIVATE_MESSAGES_ID)
            .document().set(testMsg)

        firestore.collection(GROUPS_ID).document(textGroupID)
            .update("lastSentMessage", testMsg)
    }

    fun getCollectionReference(): CollectionReference {
        return firestore.collection(GROUPS_ID)
    }

}
//9704maSB3ETKq1jF0rTtOhaUq8m2 0100
//Uhp7yfvA8HW4HIS49sQBoe7wovQ2 0155
//F0rLNHDqXZdZz6sqIeJj