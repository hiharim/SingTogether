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
        "Authorization:key=AAAAT7Xhvu8:APA91bGNcnNu6G-wq2yEv02RycxwjqIj9IyTn7X_hL0GPiHm-3x8ivyenPHAXkQsEHtVrj5SNJzrUaLcjiRGjxtAyeGZ_6k4-ZnM2UwX_Vdt5JgJ1XGbTnLZAOwPMVrsgVR_bnnjsmr8" // Your server key refer to video for finding your server key
    )
    @POST("fcm/send")
    open fun sendNotifcation(@Body body: NotificationSender?): Call<MyResponse?>?
}