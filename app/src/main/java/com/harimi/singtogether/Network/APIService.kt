package com.harimi.singtogether.Network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Part

interface APIService {

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAj8HbRjI:APA91bGkaIIO-_CkXhLxjnNZx4POyQ3qpn1RBy0JHLlXk02s-hd_OOn8cSB3BA2YTETVwUqL-rQT8vTQ4ramULMUFqSo4udWSOQz6f7Hl-Kbwc5zplenGAYPg7cVYGgrTSeGhg1pMb0w" // Your server key refer to video for finding your server key
    )
    @POST("fcm/send")
    open fun sendNotifcation(@Body body: NotificationSender?): Call<MyResponse?>?
}