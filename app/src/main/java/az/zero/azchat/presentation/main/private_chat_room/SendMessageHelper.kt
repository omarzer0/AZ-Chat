package az.zero.azchat.presentation.main.private_chat_room

import android.app.Application
import android.net.Uri
import az.zero.azchat.common.*
import az.zero.azchat.data.ApiService
import az.zero.azchat.di.remote.ApplicationScope
import az.zero.azchat.domain.models.group.Group
import az.zero.azchat.domain.models.message.Message
import az.zero.azchat.domain.models.push_notifications.NotificationData
import az.zero.azchat.domain.models.push_notifications.PushNotification
import az.zero.azchat.domain.models.status.Status
import az.zero.azchat.presentation.main.private_chat_room.MessageType.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

class SendMessageHelper @Inject constructor(
    private val sharedPreferenceManger: SharedPreferenceManger,
    private val firestore: FirebaseFirestore,
    private val application: Application,
    private val storage: FirebaseStorage,
    private val api: ApiService,
    @ApplicationScope private val scope: CoroutineScope
) {
    private var listener: ListenerRegistration? = null
    fun checkForImageOrAudioAndSend(
        messageType: MessageType,
        messageText: String,
        imageUri: Uri?,
        messageAudio: Uri?,
        gid: String,
        notificationToken: String
    ) {
        val realPath = imageUri?.let { RealPathUtil.getRealPath(application, it) } ?: ""
        val userId = sharedPreferenceManger.uid
        val userName = sharedPreferenceManger.userName
        val userImage = sharedPreferenceManger.userImage
        val otherUserNotificationToken = sharedPreferenceManger.notificationToken
        val timeInMill = System.currentTimeMillis()

        when (messageType) {
            TEXT -> {
                sendMessage(
                    messageText,
                    "",
                    "",
                    gid,
                    sharedPreferenceManger.userName,
                    notificationToken,
                    hasImage = false,
                    hasVoice = false,
                    userName,
                    userImage,
                    userId,
                    otherUserNotificationToken
                )
            }
            AUDIO -> {
                sendMessage(
                    messageText,
                    "",
                    messageAudio.toString(),
                    gid,
                    sharedPreferenceManger.userName,
                    notificationToken,
                    hasImage = false,
                    hasVoice = true,
                    userName,
                    userImage,
                    userId,
                    otherUserNotificationToken
                )
            }
            IMAGE -> {
                if (realPath.isEmpty()) return
                uploadImageByUserId(
                    application.contentResolver,
                    realPath,
                    storage.reference.child("chatImages/$userId/$timeInMill.jpg"),
                    onUploadImageSuccess = {
                        sendMessage(
                            messageText,
                            it.toString(),
                            "",
                            gid,
                            sharedPreferenceManger.userName,
                            notificationToken,
                            hasImage = true,
                            hasVoice = false,
                            userName,
                            userImage,
                            userId,
                            otherUserNotificationToken
                        )
                    },
                    onUploadImageFailed = {
                        logMe("Failed to upload! checkForImageAndSend")
                    })
            }
        }
    }

    private fun sendMessage(
        messageText: String,
        imageUri: String,
        audioUri: String,
        gid: String,
        senderName: String,
        senderDeviceToken: String,
        hasImage: Boolean,
        hasVoice: Boolean,
        otherUserName: String,
        otherUserImage: String,
        otherUserUID: String,
        otherUserNotificationToken: String
    ) {
        val randomId = firestore.collection(GROUPS_ID).document().id
        val message = Message(
            randomId,
            messageText,
            Timestamp(Date()),
            sharedPreferenceManger.uid,
            deleted = false,
            updated = false,
            loved = false,
            seen = false,
            imageUri = imageUri,
            audioUri = audioUri
        )

        logMe("repo\n$message")
        firestore.collection(MESSAGES_ID).document(gid)
            .collection(PRIVATE_MESSAGES_ID)
            .document(randomId).set(message)

        firestore.collection(GROUPS_ID).document(gid)
            .update("lastSentMessage", message)

        scope.launch {
            try {
                val notification = PushNotification(
                    NotificationData(
                        senderName,
                        messageText,
                        hasImage,
                        hasVoice,
                        gid,
                        otherUserName,
                        otherUserImage,
                        otherUserNotificationToken,
                        otherUserUID
                    ),
                    senderDeviceToken
                )
                val response = api.sendNotification(notification)
                if (response.isSuccessful) {
                    logMe("Response success ${response.body()}", "SendNotification")
                } else {
                    logMe("error  ${response.code()}", "SendNotification")
                }
            } catch (e: Exception) {
                logMe("Response error ${e.localizedMessage}", "SendNotification")
            }
        }

    }

    fun clearOtherUserStatusListener() {
        listener?.remove()
    }

    fun getOtherUserStatus(
        gid: String,
        otherUserID: String,
        onSuccess: (UserStatus) -> Unit
    ) {
        listener = firestore.collection(MESSAGES_ID).document(gid)
            .collection(USER_STATUS).document(otherUserID).addSnapshotListener { value, error ->
                if (error != null) {
                    logMe("otherUserStatus $error")
                    return@addSnapshotListener
                }
                if (value == null) return@addSnapshotListener

                val status = value.toObject<Status>() ?: return@addSnapshotListener
                val userStatus = when {
                    status.writing!! -> UserStatus.WRITING
                    status.online!! -> UserStatus.ONLINE
                    else -> UserStatus.OFFLINE
                }
                onSuccess(userStatus)
            }
    }

    fun updateCurrentUserStatus(gid: String, userStatus: UserStatus) {
        val status: Status = when (userStatus) {
            UserStatus.ONLINE -> {
                Status(writing = false, online = true)
            }
            UserStatus.WRITING -> {
                Status(writing = true, online = true)
            }
            UserStatus.OFFLINE -> {
                Status(writing = false, online = false)
            }
        }
        firestore.collection(MESSAGES_ID).document(gid)
            .collection(USER_STATUS).document(sharedPreferenceManger.uid).set(status)
    }

    fun setAllMessagesAsSeen(gid: String) {
        val uid = sharedPreferenceManger.uid
        firestore.collection(MESSAGES_ID).document(gid)
            .collection(PRIVATE_MESSAGES_ID)
            .whereEqualTo("seen", false)
            .get().addOnSuccessListener { querySnapShot ->
                querySnapShot.documents.forEach {
                    val message = it.toObject<Message>() ?: return@forEach
                    if (message.sentBy != uid) {
                        it.reference.update("seen", true)
                    }
                }
            }

        firestore.collection(GROUPS_ID).document(gid).get().addOnSuccessListener {
            val group = it.toObject<Group>() ?: return@addOnSuccessListener
            val message = group.lastSentMessage ?: return@addOnSuccessListener
            if (message.sentBy == uid) return@addOnSuccessListener
            message.seen = true
            firestore.collection(GROUPS_ID).document(gid).update("lastSentMessage", message)
        }
    }

    fun addGroup(
        gid: String,
        otherUserID: String,
        messageText: String,
        messageImage: Uri?,
        messageAudio: Uri?,
        messageType: MessageType,
        notificationToken: String,
        onSuccess: (Boolean) -> Unit
    ) {
        val uID = sharedPreferenceManger.uid
        // get random id
        val randomId = abs(Random().nextLong())
        val message = Message(
            randomId.toString(),
            messageText,
            Timestamp(Date()),
            sharedPreferenceManger.uid,
            deleted = false,
            updated = false,
            loved = false,
            seen = false,
            imageUri = messageImage?.toString() ?: "",
            audioUri = messageAudio?.toString() ?: ""
        )

        checkIfGroupExists(uID, otherUserID, onSuccess = { exists ->
            logMe("exist checkIfGroupExists $exists")
            if (!exists) {
                val newGroup = Group(
                    gid,
                    "new g1",
                    false,
                    listOf(uID, otherUserID),
                    Timestamp(Date()),
                    Timestamp(Date()),
                    uID,
                    "",
                    message,
                    firestore.document("users/$uID"),
                    firestore.document("users/$otherUserID")
                )
                firestore.collection(GROUPS_ID)
                    .document(gid)
                    .set(newGroup).addOnCompleteListener {
                        onSuccess(it.isSuccessful)
                    }
            } else {
                onSuccess(true)
            }
            logMe("add check")
            checkForImageOrAudioAndSend(
                messageType,
                messageText,
                messageImage,
                messageAudio,
                gid,
                notificationToken
            )
        })


    }

    private fun checkIfGroupExists(
        uID: String,
        otherUserID: String,
        onSuccess: (exists: Boolean) -> Unit
    ) {
        firestore.collection(GROUPS_ID)
            .whereArrayContains("members", uID)
            .get().addOnSuccessListener {
                val exists = it.any { document ->
                    val group = document.toObject<Group>()
                    if (group.hasNullField()) return@addOnSuccessListener
                    group.members!!.contains(otherUserID)
                }
                onSuccess(exists)
            }
    }
}