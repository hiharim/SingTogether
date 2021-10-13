package com.harimi.singtogether.Data

import com.google.gson.annotations.SerializedName

data class MyBroadcastData(val idx:String , val uploadUserProfile : String ,val uploadUserNickName:String, val thumbnail :String ,val replayTitle: String ,
                           val replayReviewNumber:String, val replayHits:String , val replayLikeNumber: String ,val uploadDate:String,val uploadUserEmail:String ,val liked : Boolean,val replayPostLikeIdx :String, val replayVideo:String )


