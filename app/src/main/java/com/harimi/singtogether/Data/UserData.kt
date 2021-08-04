package com.harimi.singtogether.Data

import com.google.gson.annotations.SerializedName

data class UserData(
    // 회원가입 유저 정보
    @SerializedName("email")
    val email : String,
    @SerializedName("nickname")
    val nickname : String,
//    val profile : String,
    @SerializedName("social")
    val social : String,
    @SerializedName("token")
    val token : String
)
