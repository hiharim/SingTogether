package com.harimi.singtogether.Data

import com.google.gson.annotations.SerializedName

data class DuetData(
    //10개

    @SerializedName("idx")
    val idx : Int,
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
    @SerializedName("nickname")
    val nickname : String,
    @SerializedName("profile")
    val profile : String,
    @SerializedName("duet_path")
    val duet_path : String,
    @SerializedName("date")
    val date : String,
)
