package com.harimi.singtogether.Data

import com.google.gson.annotations.SerializedName

// mr 노래 파일 데이터 클래스
// SerializedName : json 포맷 데이터로 변환된 이름
data class MRData(
        @SerializedName("idx")
        val idx : Int,
        @SerializedName("title")
        val title : String,
        @SerializedName("singer")
        val singer : String,
        @SerializedName("song_path")
        val song_path : String,
        @SerializedName("genre")
        val genre : String,
)
