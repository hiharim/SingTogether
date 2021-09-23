package com.harimi.singtogether.Network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var instance: Retrofit? = null
    private val gson = GsonBuilder().setLenient().create()
    // 서버 주소
    private const val BASE_URL = "http://3.35.236.251/"

    var okHttpClient = OkHttpClient.Builder()
        .connectTimeout(600,TimeUnit.SECONDS)
        .readTimeout(600,TimeUnit.SECONDS)
        .writeTimeout(600,TimeUnit.SECONDS)
        .build()

    // SingleTon
    fun getInstance(): Retrofit {
        if (instance == null) {
            instance = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build()
        }

        return instance!!
        // !! 의미 : Null이 값으로 들어오면 exception을 발생
    }
}