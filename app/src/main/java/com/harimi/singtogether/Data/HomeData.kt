package com.harimi.singtogether.Data

import com.google.gson.annotations.SerializedName

data class HomeData(

    // 14개
    @SerializedName("idx")
    val idx : Int,
    @SerializedName("thumbnail")
    val thumbnail : String,
    @SerializedName("title")
    val title : String,
    @SerializedName("singer")
    val singer : String,
    @SerializedName("lyrics")
    val lyrics : String,
    @SerializedName("cnt_play")
    val cnt_play : String,
    @SerializedName("cnt_reply")
    val cnt_reply : String,
    @SerializedName("cnt_like")
    val cnt_like : String,
    @SerializedName("nickname")
    val nickname : String,
    @SerializedName("profile")
    val profile : String,
    @SerializedName("song_path")
    val song_path : String,
    @SerializedName("collaboration_nickname")
    val collaboration_nickname : String,
    @SerializedName("collaboration_profile")
    val collaboration_profile : String,
    @SerializedName("date")
    val date : String,
    @SerializedName("mr_idx")
    val mr_idx : Int

//    val idx:String ,
//    val thumbnail :String ,
//    val songTitle: String ,
//    val singer:String,
//    val hits:String ,
//    val likeNumber: String ,
//    val uploadUserProfile : String ,
//    val uploadUserNickName:String ,
//    val uploadDate:String ,
//    val uploadUserEmail:String

    )