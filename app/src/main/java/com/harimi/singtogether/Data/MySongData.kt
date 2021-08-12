package com.harimi.singtogether.Data

import com.google.gson.annotations.SerializedName

data class MySongData(

    // duet 테이블 idx
    @SerializedName("idx")
    val idx : Int,
    @SerializedName("thumbnail")
    val thumbnail : String,
    @SerializedName("title")
    val title : String,
    @SerializedName("nickname")
    val nickname : String,
    @SerializedName("date")
    val date : String,
)
