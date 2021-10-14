package com.harimi.singtogether.Network

import com.harimi.singtogether.Data.UserData
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

// 서버에서 호출할 메서드를 선언하는 인터페이스
// POST 방식으로 데이터를 주고 받을 때 넘기는 변수는 Field라고 해야한다.
interface RetrofitService {

    //내노래 삭제
    @FormUrlEncoded
    @POST("deleteMySong.php")
    fun deleteMySong (
        @Field("idx") idx: Int
    ) : Call<String>

    @FormUrlEncoded
    @POST("uploadMergeVideos.php")
    fun requestUploadMergeVideo (
        @Field("mr_idx") mr_idx: Int,
        @Field("output_path") output_path :String,
        @Field("nickname") nickname :String,
        @Field("collaboration_nickname") collaborationNickname :String
    ) : Call<String>

    @Multipart
    @POST("mergeVideos.php")
    fun requestMergeVideo (
        @Part("mr_path") mr_path :String,
        @Part("merge_video_path") mergeVideoPath :String,
        @Part("merge_extract_path") mergeExtractPath :String,
        @Part file : MultipartBody.Part
    ) : Call<String>

    @FormUrlEncoded
    @POST("uploadVideo.php")
    fun requestUploadVideo (
        @Field("mr_idx") mr_idx: Int,
        @Field("finish_path") finish_path :String,
        @Field("extract_path") extract_path :String,
        @Field("nickname") nickname :String
    ) : Call<String>

    @Multipart
    @POST("mixVideo.php")
    fun requestMixVideo (
        @Part("mr_path") mr_path :String,
        @Part("side") side :String,
        @Part file : MultipartBody.Part
    ) : Call<String>

    @Multipart
    @POST("testUpload.php")
    fun requestUpload (
        @Part("mr_idx") mr_idx :Int,
        @Part("nickname") nickname :String,
        @Part file : MultipartBody.Part
    ) : Call<String>

    @Multipart
    @POST("uploadReplayVideo.php")
    fun requestUploadReplayVideo (
        @Part("userEmail") userEmail :String,
        @Part("nickname")  nickname :String,
        @Part("profile")  profile :String,
        @Part("roomTitle")  roomTitle :String,
        @Part("thumbnail")  thumbnail :String,
        @Part file : MultipartBody.Part
    ) : Call<String>

    @Multipart
    @POST("editUserProfile.php")
    fun requestEditUserProfile (
        @Part("userEmail") userEmail :String,
        @Part("nickname")  nickname :String,
        @Part file : MultipartBody.Part
    ) : Call<String>

    @FormUrlEncoded
    @POST("editUserProfile.php")
    fun requestEditUserNickname (
        @Field("userEmail") email :String,
        @Field("nickname") nickname :String
    ) : Call<String>

    @FormUrlEncoded
    @POST("join_none.php")
    fun requestJoinNone (
        @Field("email") email :String,
        @Field("nickname") nickname :String,
        @Field("social") social :String,
        @Field("token") token :String,
        @Field("profile") profile :String,
    ) : Call<String>

    @FormUrlEncoded
    @POST("autoLoginCheck.php")
    fun requestAutoLogin (
        @Field("email") email :String
    ) : Call<String>

    @FormUrlEncoded
    @POST("getDetailReplayReview.php")
    fun requestGetReplayReview (
        @Field("replayIdx") replayIdx :String
    ) : Call<String>


    @FormUrlEncoded
    @POST("WriteReview.php")
    fun requestWriteReview (
        @Field("replayIdx") replayIdx :String,
        @Field("uploadUserEmail") uploadUserEmail :String,
        @Field("uploadUserProfile") uploadUserProfile :String,
        @Field("uploadUserNickname") uploadUserNickname :String,
        @Field("review") review :String,
        @Field("uploadDate") uploadDate :String
    ) : Call<String>

    @FormUrlEncoded
    @POST("liveStreamingCheck.php")
    fun requestLiveStreamingCheck (
        @Field("roomIdx") roomIdx :String
    ) : Call<String>

    @FormUrlEncoded
    @POST("searchLiveStreaming.php")
    fun requestSearchLiveStreaming (
        @Field("searchInput") searchInput :String
    ) : Call<String>

    @FormUrlEncoded
    @POST("editReplayReview.php")
    fun requestEditReplayReview (
        @Field("idx") idx :String,
        @Field("editReview") editReview :String
    ) : Call<String>

    @FormUrlEncoded
    @POST("deleteReplayReview.php")
    fun requestDeleteReplayReview (
        @Field("idx") idx :String,
    ) : Call<String>

    @FormUrlEncoded
    @POST("updateReplayHIts.php")
    fun requestUpdateReplayHIts (
        @Field("idx") idx :String
    ) : Call<String>

    @FormUrlEncoded
    @POST("OutViewer.php")
    fun requestOutViewer (
        @Field("roomIdx") roomIdx :String
    ) : Call<String>

    @POST("loadHomePost.php")
    fun requestGetHomePost (
    ) : Call<String>
    @POST("loadHomePost2.php")
    fun requestGetHomePost2 (
    ) : Call<String>



    @POST("loadLiveStreamingPost.php")
    fun requestGetLiveStreamingPost (
    ) : Call<String>

    @FormUrlEncoded
    @POST("finishLiveStreamingPost.php")
    fun requestFinishLiveStreamingPost (
        @Field("roomIdx") roomIdx :String,
    ) : Call<String>

    @FormUrlEncoded
    @POST("loadReplayPost.php")
    fun requestGetReplayPost (
        @Field("userEmail") userEmail :String,
    ) : Call<String>

    @POST("loadMR.php")
    fun requestMR(
    ):Call<String>

    // 가사 시간 데이터 불러오기
    @FormUrlEncoded
    @POST("loadLyrics.php")
    fun requestLyrics(
        @Field("mr_idx") mr_idx :String,
    ):Call<String>

    @POST("loadDuet.php")
    fun requestDuet(
    ):Call<String>

    @FormUrlEncoded
    @POST("loadMySong.php")
    fun requestMySong(
        @Field("nickname") token :String,
    ):Call<String>

    @FormUrlEncoded
    @POST("clickReplayLike.php")
    fun requestClickLike(
        @Field("ReplayIdx") roomIdx :String,
        @Field("userEmail") userEmail :String,
        @Field("clickDate") clickDate :String
    ):Call<String>

    @FormUrlEncoded
    @POST("cancelReplayLike.php")
    fun requestCancelLike(
        @Field("ReplayIdx") roomIdx :String,
        @Field("userEmail") userEmail :String,
        @Field("replayPostLikeIdx") replayPostLikeIdx :String
    ):Call<String>

    @FormUrlEncoded
    @POST("loadMyBroadcast.php")
    fun requestMyBroadcast(
       @Field("userEmail") userEmail :String
    ):Call<String>

    @Multipart
    @POST("join.php")
    fun requestJoin (
        @Part("email") email :String,
        @Part("nickname") nickname :String,
        @Part("social") social :String,
        @Part("token") token :String,
        @Part file : MultipartBody.Part,
    ) : Call<String>

    @Multipart
    @POST("LiveStreamingStart.php")
    fun requestLiveStreamingStart (
        @Part("email") email :String,
        @Part("nickname") nickname :String,
        @Part("profile") profile :String,
        @Part("liveTitle") liveTitle :String,
        @Part file : MultipartBody.Part,
    ) : Call<String>

}