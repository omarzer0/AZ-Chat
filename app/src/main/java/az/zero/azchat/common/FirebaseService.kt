package az.zero.azchat.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import az.zero.azchat.R
import az.zero.azchat.presentation.main.MainActivity
import az.zero.azchat.repository.MainRepositoryImpl
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import javax.inject.Inject
import kotlin.random.Random

class FirebaseService : FirebaseMessagingService() {

    @Inject
    lateinit var repositoryImpl: MainRepositoryImpl

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        tryNow { repositoryImpl.updateUserToken(newToken) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        logMe("onMessageReceived: ${message.data}")
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        val notificationID: Int = try {
            message.senderId?.toLong()?.toInt() ?: Random.nextInt()
        } catch (e: Exception) {
            Random.nextInt()
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT)
        val contentText = if (message.data["hasImage"] == "true")
            "${message.data["message"]}\nSent an Image"
        else
            "${message.data["message"]}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE_HIGH).apply {
            description = DESCRIPTION
            enableLights(true)
            lightColor = NOTIFICATION_LIGHT_COLOR
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "my_channel"
        private const val CHANNEL_NAME = "Chat Messages"
        private const val DESCRIPTION = "Chat Messages Channel"
        private const val NOTIFICATION_LIGHT_COLOR = Color.GREEN
    }
}











