package com.harimi.singtogether.Data

import com.google.gson.annotations.SerializedName

data class MySongData(

    @SerializedName("duet_idx")
    val duet_idx : Int,
    @SerializedName("mr_idx")
    val mr_idx : Int,
    @SerializedName("thumbnail")
    val thumbnail : String,
    @SerializedName("title")
    val title : String,
    @SerializedName("singer")
    val singer : String,
    @SerializedName("cnt_play")
    val cnt_play : String,
    @SerializedName("cnt_reply")
    val cnt_reply : String,
    @SerializedName("cnt_duet")
    val cnt_duet : String,
    @SerializedName("email")
    val email : String,
    @SerializedName("nickname")
    val nickname : String,
    @SerializedName("profile")
    val profile : String,
    @SerializedName("duet_path")
    val duet_path : String,
    @SerializedName("date")
    val date : String,
    @SerializedName("mr_path")
    val mr_path : String,
    @SerializedName("extract_path")
    val extract_path : String,
    @SerializedName("kinds")
    val kinds: String,
    @SerializedName("lyrics")
    val lyrics: String,
    @SerializedName("token")
    val token: String
)
