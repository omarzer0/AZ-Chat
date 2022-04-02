package az.zero.azchat.common

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import az.zero.azchat.R
import az.zero.azchat.common.SharedPreferenceManger.Companion.CURRENT_GID
import az.zero.azchat.common.SharedPreferenceManger.Companion.LOGGED_IN
import az.zero.azchat.common.SharedPreferenceManger.Companion.SHARED_PREFERENCES_NAME
import az.zero.azchat.domain.models.group.Group
import az.zero.azchat.domain.models.private_chat.PrivateChat
import az.zero.azchat.domain.models.user.User
import az.zero.azchat.presentation.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FirebaseService : FirebaseMessagingService() {

    @Inject
    lateinit var repositoryImpl: NotificationTokenHelper

    private var sharedPreference: SharedPreferences? = null

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        tryNow(tag = "updateUserToken") {
            // First time the uid is empty so an exception will be thrown
            // Document references must have an even number of segments, but users has 1
            logMe("newToken= $newToken", "updateUserToken")
            repositoryImpl.updateUserToken(newToken)
        }

    }

    // To be able to inject override onCreate
    override fun onCreate() {
        super.onCreate()
        logMe("FirebaseService onCreate")
        sharedPreference = this.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (sharedPreference == null) {
            sharedPreference = this.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        }

        if (shouldNotContinue(message)) return

        val argsData = getArgsData(message)

        val contentText = when {
            message.data["hasImage"] == "true" -> "${message.data["message"]}\nSent an image"
            message.data["hasVoice"] == "true" -> {
                "Sent an audio"
            }
            else -> "${message.data["message"]}"
        }

        val isGroup = message.data["isGroup"] ?: ""
        val title = if (isGroup == "true") message.data["groupName"] ?: ""
        else message.data["username"] ?: ""

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent(argsData))
            .build()

        val notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifyManager.notify(getNotificationId(message), notification)

    }

    private fun shouldNotContinue(message: RemoteMessage): Boolean {
        var shouldNotContinue = false
        val isNotificationsDisabled =
            message.data["gid"] == sharedPreference!!.getString(CURRENT_GID, "")
        if (isNotificationsDisabled) shouldNotContinue = true

        val hasLoggedIn = sharedPreference!!.getBoolean(LOGGED_IN, false)
        if (!hasLoggedIn) shouldNotContinue = true

        return shouldNotContinue
    }

    private fun getArgsData(message: RemoteMessage): Bundle {
        return Bundle().apply {
            val gid = message.data["gid"] ?: ""
            val username = message.data["username"] ?: ""
            val userImage = message.data["userImage"] ?: ""

            val groupName = message.data["groupName"] ?: ""
            val groupImage = message.data["groupImage"] ?: ""

            val notificationToken = message.data["notificationToken"] ?: ""
            val otherUserUID = message.data["otherUserUID"] ?: ""

            putParcelable(
                "privateChat", PrivateChat(
                    Group(
                        gid = gid,
                        name = groupName,
                        image = groupImage,
                        groupNotificationTopic = notificationToken,
                        ofTypeGroup = message.data["isGroup"] == "true"
                    ), User(
                        uid = otherUserUID,
                        name = username,
                        imageUrl = userImage,
                        notificationToken = notificationToken,
                    ), gid
                )
            )
        }

    }

    private fun getNotificationId(message: RemoteMessage) = try {
        val gid = message.data["gid"] ?: ""
        var sCode = ""
        gid.mapIndexed { index, c ->
            if (index > 8) return@mapIndexed
            sCode += c.code.rem(9)
        }
        sCode.toInt()
    } catch (e: Exception) {
        logMe("Failed=> ${e.localizedMessage}", "notificationIDForApp")
        Random.nextInt()
    }


    private fun pendingIntent(args: Bundle): PendingIntent {
        return NavDeepLinkBuilder(applicationContext)
            .setGraph(R.navigation.main_nav_graph)
            .setComponentName(MainActivity::class.java)
            .setDestination(R.id.privateChatRoomFragment)
            .setArguments(args)
            .createPendingIntent()
    }

    companion object {
        const val CHANNEL_ID = "my_channel"
        const val CHANNEL_NAME = "Chat Messages"
        const val DESCRIPTION = "Chat Messages Channel"
        const val NOTIFICATION_LIGHT_COLOR = Color.GREEN
    }
}











