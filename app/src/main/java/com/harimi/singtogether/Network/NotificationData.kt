package com.harimi.singtogether.Network

data class NotificationData (
    val title: String,
    val message: String,
    val replayIdx: String,
    val uploadUserEmail: String,
    val uploadUserProfile: String,
    val uploadUserNickName: String,
    val thumbnail: String,
    val uploadDate: String,
    val replayTitle: String,
    val replayLikeNumber: String,
    val replayHits: String,
    val replayReviewNumber: String,
    val replayPostLikeIdx: String,
    val liked: Boolean,
    val replayVideo: String,
    val uploadUserFCMToken: String,
    val category : String

)