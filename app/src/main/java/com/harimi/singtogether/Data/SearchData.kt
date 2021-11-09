package com.harimi.singtogether.Data

class SearchData {

    var getType: String ? = null
    var idx: String? = null
    var email: String? = null
    var thumbnail: String? = null
    var nickName: String? = null
    var profile: String? = null
    var title: String? = null
    var viewer: String? = null
    var replayTitle: String? = null
    var uploadUserEmail: String? = null
    var uploadUserProfile: String? = null
    var uploadUserNickName: String? = null
    var replayLikeNumber: String? = null
    var replayHits: String? = null
    var uploadDate: String? = null
    var replayVideo: String? = null
    var replayReviewNumber: String? = null
    var time: String? = null
    var uploadUserFCMToken: String? = null
    var liked: Boolean? = false
    var isBadge: Boolean? = null
    var replayPostLikeIdx: String? = null



    constructor(getType : String , idx: String, thumbnail: String, replayTitle: String, uploadUserEmail: String, uploadUserProfile: String, uploadUserNickName: String, replayLikeNumber: String
                , replayHits: String, uploadDate: String, replayVideo: String, replayReviewNumber: String, time: String, uploadUserFCMToken: String, d: Boolean, isBadge: Boolean, replayPostLikeIdx: String) {
        this.getType = getType
        this.idx = idx
        this.thumbnail = thumbnail
        this.replayTitle = replayTitle
        this.uploadUserEmail = uploadUserEmail
        this.uploadUserProfile = uploadUserProfile
        this.uploadUserNickName = uploadUserNickName
        this.replayLikeNumber = replayLikeNumber
        this.replayHits = replayHits
        this.uploadDate = uploadDate
        this.replayVideo = replayVideo
        this.replayReviewNumber = replayReviewNumber
        this.time =time
        this.uploadUserFCMToken = uploadUserFCMToken
        this.liked = liked
        this.isBadge = isBadge
        this.replayPostLikeIdx = replayPostLikeIdx

    }

    constructor( getType : String , idx: String, email: String, thumbnail: String, nickName: String, profile: String, title: String, viewer: String) {
        this.getType = getType
        this.idx = idx
        this.email = email
        this.thumbnail = thumbnail
        this.nickName = nickName
        this.profile = profile
        this.title = title
        this.viewer = viewer

    }


}