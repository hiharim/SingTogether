package com.harimi.singtogether.Network

import com.harimi.singtogether.Data.UserData
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

// 서버에서 호출할 메서드를 선언하는 인터페이스
// POST 방식으로 데이터를 주고 받을 때 넘기는 변수는 Field라고 해야한다.
interface RetrofitService {

    @FormUrlEncoded
    @Headers(
            "accept: application/json",
            "content-type: application/x-www-form-urlencoded; charset=utf-8")
    @POST("/sign_in.js")
    fun requestSignIn (
        @Field("email") email :String,
        @Field("nickname") nickname :String,
//        @Field("profile") profile :String,
        @Field("social") social :String,
        @Field("token") token :String,
    ) : Call<String>

}