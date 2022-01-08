package az.zero.azchat.repository

import android.util.Log
import az.zero.azchat.common.*
import az.zero.azchat.data.models.group.Group
import az.zero.azchat.data.models.message.Message
import az.zero.azchat.data.models.private_chat.PrivateChat
import az.zero.azchat.data.models.user.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class MainRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val sharedPreferenceManger: SharedPreferenceManger,
) {
    private val TAG = "tag"

    fun getAllUsers(
        onGetUsersDone: (List<User>) -> Unit,
    ) {
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

    fun getPrivateChatsForUser(
        onSuccess: (List<PrivateChat>) -> Unit
    ) {
        val uid = sharedPreferenceManger.uid
        firestore.collection(GROUPS_ID)
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
                    getUser(group, otherUserId, onSuccess)
                }
            }
    }

    private fun getUser(
        group: Group, uid: String,
        onSuccess: (List<PrivateChat>) -> Unit
    ) {
        firestore.document(uid).get().addOnSuccessListener {
            if (it.exists()) {
                val user = it.toObject<User>() ?: return@addOnSuccessListener
                onSuccess(listOf(PrivateChat(group, user)))
            }
        }
    }

    private fun getImageByUID(uid: String, onSuccess: (String) -> Unit) {
        val imageRef = storage.reference.child("profileImages/$uid.jpg")
        imageRef.downloadUrl.addOnSuccessListener {
            logMe(it.toString())
            onSuccess(it.toString())
        }
    }


    fun getMessagesQuery(gid: String): Query {
        return firestore.collection(MESSAGES_ID)
            .document(gid)
            .collection(PRIVATE_MESSAGES_ID)
            .orderBy("sentAt", Query.Direction.ASCENDING)
    }

    fun getUID() = sharedPreferenceManger.uid

    fun getMessagesByGroupId(
        gid: String,
        onSuccess: (List<Message>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection(MESSAGES_ID)
            .document(gid)
            .collection(PRIVATE_MESSAGES_ID)
            .orderBy("sentAt")
            .get()
            .addOnSuccessListener { docs ->
                val messages = ArrayList<Message>()
                docs.forEach { doc ->
                    val message = doc.toObject<Message>()
                    if (!message.hasNullField()) {
                        messages.add(message)
                    }
                }
                onSuccess(messages)
            }
            .addOnFailureListener {
                onFailure(it)
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

    fun sendMessage(messageText: String, gid: String) {
        val randomId = abs(Random().nextLong())
//        val textGroupID = "F0rLNHDqXZdZz6sqIeJj"
//        val textOmarUserID = "9704maSB3ETKq1jF0rTtOhaUq8m2"
//        val testMessageText = "Fine Thx!"
//
        val message = Message(
            randomId.toString(),
            messageText,
            Timestamp(Date()),
            sharedPreferenceManger.uid,
            deleted = false,
            updated = false,
            loved = false
        )

        logMe("repo\n$message")
        firestore.collection(MESSAGES_ID).document(gid)
            .collection(PRIVATE_MESSAGES_ID)
            .document().set(message)

        firestore.collection(GROUPS_ID).document(gid)
            .update("lastSentMessage", message)
    }

    fun getCollectionReference(): CollectionReference {
        return firestore.collection(GROUPS_ID)
    }

}
//9704maSB3ETKq1jF0rTtOhaUq8m2 0100
//Uhp7yfvA8HW4HIS49sQBoe7wovQ2 0155
//F0rLNHDqXZdZz6sqIeJj