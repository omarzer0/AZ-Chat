package az.zero.azchat.core

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application(){

//    override fun onCreate() {
//        super.onCreate()
//        createNotificationChannels()
//    }

//    private fun createNotificationChannels() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            var mediaChannel = NotificationChannel(
//                MEDIA_CHANNEL_ID,
//                "Quran App",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            mediaChannel.audioAttributes
//            mediaChannel.description
//
//            var manger: NotificationManager = getSystemService(NotificationManager::class.java)
//            manger.createNotificationChannel(mediaChannel)
//        }
//    }
}