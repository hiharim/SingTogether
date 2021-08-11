package com.harimi.singtogether.Network

import com.harimi.singtogether.Data.UserData
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

// 서버에서 호출할 메서드를 선언하는 인터페이스
// POST 방식으로 데이터를 주고 받을 때 넘기는 변수는 Field라고 해야한다.
interface RetrofitService {

//    @FormUrlEncoded
//    @Headers(
//            "accept: application/json",
//            "content-type: application/x-www-form-urlencoded; charset=utf-8")
//    @POST("/sign_in.js")
//    fun requestSignIn (
//        @Field("email") email :String,
//        @Field("nickname") nickname :String,
////        @Field("profile") profile :String,
//        @Field("social") social :String,
//        @Field("token") token :String,
//    ) : Call<String>


    @FormUrlEncoded
    @POST("join_none.php")
    fun requestJoinNone (
        @Field("email") email :String,
        @Field("nickname") nickname :String,
        @Field("social") social :String,
        @Field("token") token :String,
        @Field("profile") profile :String,
    ) : Call<String>

    @FormUrlEncoded
    @POST("autoLoginCheck.php")
    fun requestAutoLogin (
        @Field("email") email :String

    ) : Call<String>

    @POST("loadHomePost.php")
    fun requestGetHomePost (

    ) : Call<String>

    @Multipart
    @POST("join.php")
    fun requestJoin (
        @Part("email") email :String,
        @Part("nickname") nickname :String,
        @Part("social") social :String,
        @Part("token") token :String,
        @Part file : MultipartBody.Part,
    ) : Call<String>

}