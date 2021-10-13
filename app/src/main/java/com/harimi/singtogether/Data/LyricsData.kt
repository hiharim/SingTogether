package com.harimi.singtogether.Data

import com.google.gson.annotations.SerializedName

data class LyricsData(
    @SerializedName("seconds")
    val seconds : String,
//    @SerializedName("next_seconds")
//    val next_seconds : String,
    @SerializedName("line")
    val line : String,
    
)
