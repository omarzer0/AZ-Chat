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
        imageUri: String?,
        messageAudio: String?,
        gid: String,
        notificationToken: String,
        audioDuration: Long = -1,
        messageId: String? = null,
        isGroup: Boolean,
        groupName: String,
        groupImage: String
    ) {
        val realPath = imageUri?.let { RealPathUtil.getRealPath(application, Uri.parse(it)) } ?: ""
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
                    otherUserNotificationToken,
                    audioDuration,
                    messageId,
                    isGroup,
                    groupName,
                    groupImage
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
                    otherUserNotificationToken,
                    audioDuration,
                    messageId,
                    isGroup,
                    groupName,
                    groupImage
                )
            }
            IMAGE -> {
                if (realPath.isEmpty()) return
                val randomId = messageId ?: firestore.collection(GROUPS_ID).document().id
                sendFakeTempImage(
                    userId,
                    userName,
                    realPath,
                    gid,
                    randomId,
                    isGroup,
                    groupName,
                    groupImage
                )
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
                            otherUserNotificationToken,
                            audioDuration,
                            randomId,
                            isGroup,
                            groupName,
                            groupImage
                        )
                    },
                    onUploadImageFailed = {
                        logMe("Failed to upload! checkForImageAndSend")
                    })
            }
        }
    }

    private fun sendFakeTempImage(
        userId: String,
        userName: String,
        userImage: String,
        gid: String,
        messageId: String,
        isGroup: Boolean,
        groupName: String,
        groupImage: String
    ) {
        sendMessage(
            "",
            userImage,
            "",
            gid,
            sharedPreferenceManger.userName,
            "",
            hasImage = true,
            hasVoice = false,
            userName,
            userImage,
            userId,
            "",
            messageId = messageId,
            audioDuration = 0,
            isGroup = isGroup,
            groupName = groupName,
            groupImage = groupImage
        )
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
        otherUserNotificationToken: String,
        audioDuration: Long,
        messageId: String? = null,
        isGroup: Boolean,
        groupName: String,
        groupImage: String
    ) {
        val randomId = messageId ?: firestore.collection(GROUPS_ID).document().id
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
            audioUri = audioUri,
            audioDuration = audioDuration,
            senderName = senderName
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
                        otherUserUID,
                        isGroup,
                        groupName,
                        groupImage
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
        messageImage: String?,
        messageAudio: String?,
        messageType: MessageType,
        notificationToken: String,
        onSuccess: (Boolean) -> Unit,
        audioDuration: Long = -1,
        isGroup: Boolean,
        groupName: String,
        groupImage: String
    ) {
        logMe("exist checkIfGroupExists add group", "checkIfGroupExists")
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
            imageUri = messageImage ?: "",
            audioUri = messageAudio ?: "",
            audioDuration
        )

        checkIfGroupExists(uID, otherUserID, onSuccess = { exists ->
            logMe("exist checkIfGroupExists $exists", "checkIfGroupExists")
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
                    firestore.document("users/$uID").path,
                    firestore.document("users/$otherUserID").path
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
                notificationToken,
                isGroup = isGroup,
                groupName = groupName,
                groupImage = groupImage
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
                    if (group.ofTypeGroup!!) false
                    else group.members!!.contains(otherUserID)
                }
                logMe("exists=> $exists", "checkIfGroupExists")
                onSuccess(exists)
            }
    }
}