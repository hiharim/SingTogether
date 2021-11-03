package com.harimi.singtogether.Network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


import com.harimi.singtogether.Network.Constants.Companion.CONTENT_TYPE
import com.harimi.singtogether.Network.Constants.Companion.SERVER_KEY

interface NotificationAPI {

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postDuetNotification(
        @Body notification: PushDuetNotification
    ): Response<ResponseBody>

    @Headers("Authorization: key=$SERVER_KEY", "Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postSongPostNotification(
        @Body notification: PushSongPostNotification
    ): Response<ResponseBody>
}