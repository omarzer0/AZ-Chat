package az.zero.azchat.data

import az.zero.azchat.BuildConfig
import az.zero.azchat.common.CONTENT_TYPE
import az.zero.azchat.domain.models.push_notifications.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @Headers("Authorization: key=${BuildConfig.SERVER_KEY}", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun sendNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>

}