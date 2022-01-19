package az.zero.azchat.repository

import android.util.Log
import az.zero.azchat.common.*
import az.zero.azchat.data.models.group.Group
import az.zero.azchat.data.models.message.Message
import az.zero.azchat.data.models.private_chat.PrivateChat
import az.zero.azchat.data.models.user.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val sharedPreferenceManger: SharedPreferenceManger,
) {
    private val TAG = "tag"
    private var privateChatsListener: ListenerRegistration? = null

    fun getRandomFirebaseGID() = firestore.collection(GROUPS_ID).document().id
    fun removePrivateChatsListener() {
        privateChatsListener?.remove()
    }

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

    fun checkIfGroupExists(
        uID: String,
        otherUserID: String,
        onSuccess: (String) -> Unit
    ) {
        firestore.collection(GROUPS_ID)
            .whereArrayContains("members", uID)
            .get().addOnSuccessListener {
                val group = it.find { document ->
                    val group = document.toObject<Group>()
                    if (group.hasNullField()) return@addOnSuccessListener
                    group.members!!.contains(otherUserID)
                }
                group?.let { document ->
                    val existGroup = document.toObject<Group>()
                    if (existGroup.hasNullField()) return@addOnSuccessListener
                    onSuccess(existGroup.gid!!)
                } ?: onSuccess("")
            }
    }

    fun addFakeUser() {

        val fakeUser = User(
            "uid1234",
            "fakeUser",
            "",
            "bio1234",
            emptyList(),
            "+201010101010"
        )
        firestore.collection(USERS_ID).document("uid1234").set(fakeUser)
            .addOnSuccessListener {
                logMe("Add fake user success")
            }.addOnFailureListener {
                logMe(it.localizedMessage ?: "addUser error")
            }
    }

    fun getPrivateChatsForUser(
        onSuccess: (PrivateChat) -> Unit
    ) {
        val uid = sharedPreferenceManger.uid
        privateChatsListener?.remove()
        privateChatsListener = firestore.collection(GROUPS_ID)
            .whereArrayContains("members", uid).addSnapshotListener { value, error ->
                if (error != null) {
                    logMe("listenForGroupChanges $error")
                    return@addSnapshotListener
                }

                logMe("document from cache ${value?.metadata?.isFromCache} ${value?.size()}")
                value?.forEach { document ->
                    val group = document.toObject<Group>()
                    if (group.ofTypeGroup == true) return@forEach
                    if (group.hasNullField()) return@forEach
                    val otherUserId =
                        if (!group.user1!!.path.contains(uid)) group.user1!!.path
                        else group.user2!!.path
                    if (document.metadata.isFromCache) {
                        getUser(group, otherUserId, onSuccess, Source.CACHE)
                    } else {
                        getUser(group, otherUserId, onSuccess, Source.SERVER)
                    }
                }
//                firestore.enableNetwork()
            }
    }

    private fun getUser(
        group: Group, uid: String,
        onSuccess: (PrivateChat) -> Unit,
        from: Source
    ) {
        firestore.document(uid).get(from).addOnSuccessListener {
            if (it.exists()) {
                val user = it.toObject<User>() ?: return@addOnSuccessListener
                onSuccess(PrivateChat(group, user))
            }

            logMe("user from cache ${it.metadata.isFromCache}")
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
//            emptyMap(),
            Message()
        )
        firestore.collection(GROUPS_ID)
            .document(gid)
            .set(newGroup)
    }

//    fun sendMessage(messageText: String, gid: String) {
//        val randomId = abs(Random().nextLong())
//        val message = Message(
//            randomId.toString(),
//            messageText,
//            Timestamp(Date()),
//            sharedPreferenceManger.uid,
//            deleted = false,
//            updated = false,
//            loved = false
//        )
//
//        logMe("repo\n$message")
//        firestore.collection(MESSAGES_ID).document(gid)
//            .collection(PRIVATE_MESSAGES_ID)
//            .document().set(message)
//
//        firestore.collection(GROUPS_ID).document(gid)
//            .update("lastSentMessage", message)
//    }

    fun getCollectionReference(): CollectionReference {
        return firestore.collection(GROUPS_ID)
    }

    suspend fun getUserInfo(onSuccess: (User) -> Unit) {
        val uid = sharedPreferenceManger.uid
        tryAsyncNow {
            val user = firestore.collection(USERS_ID)
                .document(uid).get().await().toObject<User>() ?: return@tryAsyncNow
            if (user.hasNullField()) return@tryAsyncNow
            onSuccess(user)
        }

        firestore.collection(USERS_ID).document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                logMe("getUserInfo $error")
                return@addSnapshotListener
            }
            if (value == null) return@addSnapshotListener
            val user = value.toObject<User>() ?: return@addSnapshotListener
            onSuccess(user)
        }
    }


//    suspend fun getPrivates(): List<PrivateChat> {
//        val list = mutableListOf<PrivateChat>()
//        withContext(Dispatchers.IO) {
//            Firebase.firestore.collection(GROUPS_ID)
//                .whereArrayContains("members", "uid").get().await().forEach {
//                    val g = it.toObject<Group>()
//                    val otherUserId = if (!g.user1!!.path.contains("uid"))
//                        g.user1!!.path else g.user2!!.path
//                    val u = Firebase.firestore.collection(USERS_ID)
//                        .document(otherUserId).get().await().toObject<User>() ?: return@forEach
//                    list.add(PrivateChat(g, u, g.gid!!.toLong()))
//                }
//        }
//        return list.toList()
//    }


    //    @ExperimentalCoroutinesApi
//    fun getPrivateChatsForUser2(): Flow<List<PrivateChat>> {
//        return callbackFlow {
//            val privateChats = mutableListOf<PrivateChat>()
//            val uid = sharedPreferenceManger.uid
//            val listener = firestore.collection(GROUPS_ID).whereArrayContains("members", uid)
//                .addSnapshotListener { value, error ->
//                    if (error != null) {
//                        logMe("listenForGroupChanges $error")
//                        return@addSnapshotListener
//                    }
//
//                    val gList = mutableListOf<Group>()
//                    value?.forEach { document ->
//                        val group = document.toObject<Group>()
//                        if (group.ofTypeGroup == true) return@forEach
//                        if (group.hasNullField()) return@forEach
//                        gList.add(group)
//                    }
//
//                    gList.forEach { group ->
//                        val otherUserId = if (!group.user1!!.path.contains(uid)) group.user1!!.path
//                        else group.user2!!.path
//
//                        firestore.document(otherUserId).get().addOnSuccessListener {
//                            if (it.exists()) {
//                                val user = it.toObject<User>() ?: return@addOnSuccessListener
//                                privateChats.add(PrivateChat(group, user, 12))
//                            }
//                        }
//                    }
//
//                    trySend(privateChats)
//                }
//
//            awaitClose {
//                listener.remove()
//            }
//        }
//
//    }
}

//9704maSB3ETKq1jF0rTtOhaUq8m2 0100
//Uhp7yfvA8HW4HIS49sQBoe7wovQ2 0155
//XkfoWUX1p3VWMzGzwk9khqEQ0yG2 EMU
//F0rLNHDqXZdZz6sqIeJj