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
import az.zero.azchat.common.SharedPreferenceManger.Companion.SHARED_PREFERENCES_NAME
import az.zero.azchat.domain.models.group.Group
import az.zero.azchat.domain.models.private_chat.PrivateChat
import az.zero.azchat.domain.models.user.User
import az.zero.azchat.presentation.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.scopes.ServiceScoped
import javax.inject.Inject
import kotlin.random.Random

@ServiceScoped
class FirebaseService : FirebaseMessagingService() {

    @Inject
    lateinit var repositoryImpl: NotificationTokenHelper

    private var sharedPreference: SharedPreferences? = null

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        tryNow(tag = "updateUserToken") {
            logMe("service updateUserToken", "updateUserToken")
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

        var isNotificationsDisabled = false
        if (sharedPreference != null)
            isNotificationsDisabled =
                message.data["gid"] == sharedPreference!!.getString(CURRENT_GID, "")

        if (isNotificationsDisabled) return



        logMe("onMessageReceived: ${message.data}")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val notificationID: Int = try {
            message.senderId?.toLong()?.toInt() ?: Random.nextInt()
        } catch (e: Exception) {
            Random.nextInt()
        }

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

        notificationManager.notify(notificationID, notification)

    }

    private fun getArgsData(message: RemoteMessage): Bundle {
        return Bundle().apply {
            val gid = message.data["gid"] ?: ""
            val username = message.data["username"] ?: ""
            val userImage = message.data["userImage"] ?: ""

            val groupName = message.data["groupName"] ?: ""
            val groupImage = message.data["groupImage"] ?: ""

            val notificationToken = message.data["notificationToken"] ?: ""
//            val otherUserUID = message.data["otherUserUID"] ?: ""

//            val user: User
//            val group: Group
//
//            if (isGroup == "true") {
//                logMe(isGroup,"getArgsData")
//                group = Group(
//                    gid = gid,
//                    name = username,
//                    image = userImage,
//                    groupNotificationTopic = notificationToken,
//                    ofTypeGroup = true
//                )
//                user = User(name = "", imageUrl = "", notificationToken = "")
//
//            } else {
//                group = Group(
//                    gid = gid,
//                    name = "",
//                    image = "",
//                    groupNotificationTopic = "",
//                    ofTypeGroup = false
//                )
//                user = User(
//                    name = username,
//                    imageUrl = userImage,
//                    notificationToken = notificationToken,
//                )
//            }
//            putParcelable("privateChat", PrivateChat(group, user, gid))
            putParcelable(
                "privateChat", PrivateChat(
                    Group(
                        gid = gid,
                        name = groupName,
                        image = groupImage,
                        groupNotificationTopic = notificationToken,
                        ofTypeGroup = true
                    ), User(
                        name = username,
                        imageUrl = userImage,
                        notificationToken = notificationToken,
                    ), gid
                )
            )
        }

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











