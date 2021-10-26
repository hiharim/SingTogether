package com.harimi.singtogether.Network

import com.harimi.singtogether.Data.UserData
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

// 서버에서 호출할 메서드를 선언하는 인터페이스
// POST 방식으로 데이터를 주고 받을 때 넘기는 변수는 Field라고 해야한다.
interface RetrofitService {

    //검색
    @FormUrlEncoded
    @POST("loadSearchResult.php")
    fun loadSearchResult(
        @Field("searchInput") searchInput : String,
        @Field("which") which : String // 어떤프래그먼트이냐 resultMR,resultSong,resultDuet 구분해주는 변수
        // which 변수 왜필요하냐-서버에서 구분시켜서보내주기위해서
    ) : Call<String>

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
        @Field("email") email :String,
        @Field("nickname") nickname :String,
        @Field("collaboration_nickname") collaborationNickname :String,
        @Field("collabo_email") collabo_email :String,
        @Field("kinds") kinds :String
    ) : Call<String>

    @Multipart
    @POST("mergeVideos.php")
    fun requestMergeVideo (
        @Part("mr_path") mr_path :String,
        @Part("merge_video_path") mergeVideoPath :String,
        @Part("merge_extract_path") mergeExtractPath :String,
        @Part file : MultipartBody.Part
    ) : Call<String>

    @Multipart
    @POST("mergeAudios.php")
    fun requestMergeAudio (
        @Part("mr_path") mr_path :String,
        @Part("original_email") original_email :String,
        @Part("collaboration_email") collaboration_email :String,
        @Part("merge_extract_path") mergeExtractPath :String,
        @Part file : MultipartBody.Part
    ) : Call<String>

    @FormUrlEncoded
    @POST("uploadVideo.php")
    fun requestUploadVideo (
        @Field("mr_idx") mr_idx: Int,
        @Field("finish_path") finish_path :String,
        @Field("extract_path") extract_path :String,
        @Field("nickname") nickname :String,
        @Field("email") email :String,
        @Field("kinds") kinds :String,
        @Field("with") with :String // 솔로,듀엣 구분위해서
    ) : Call<String>

    @Multipart
    @POST("mixVideo.php")
    fun requestMixVideo (
        @Part("mr_path") mr_path :String,
        @Part("side") side :String, // 전면카메라인지 후면카메라인지
        @Part file : MultipartBody.Part
    ) : Call<String>

    @FormUrlEncoded
    @POST("uploadAudio.php")
    fun requestUploadAudio (
        @Field("mr_idx") mr_idx: Int,
        @Field("thumbnail_path") thumbnail_path :String,
        @Field("song_path") song_path :String,
        @Field("extract_path") extract_path :String,
        @Field("nickname") nickname :String,
        @Field("email") email :String,
        @Field("kinds") kinds :String, // 녹음,녹화 구분위해서
        @Field("with") with :String // 솔로,듀엣 구분위해서
    ) : Call<String>

    @Multipart
    @POST("mixAudio.php")
    fun requestMixAudio (
        @Part("mr_path") mr_path :String,
        @Part("email") email: String,
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
        @Part("time")  time :String,
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
    @POST("getMainPostReview.php")
    fun requestGetMainPostReview (
        @Field("postIdx") postIdx: String
    ) : Call<String>

    @FormUrlEncoded
    @POST("getDetailDuetReview.php")
    fun requestGetDetailDuetReview (
        @Field("detailDuetIdx") detailDuetIdx: String
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
    @POST("AddFollow.php")
    fun requestAddFollow (
        @Field("followingUser") followingUser :String,
        @Field("follower") follower :String,

    ) : Call<String>

    @FormUrlEncoded
    @POST("DeleteFollow.php")
    fun requestDeleteFollow (
        @Field("followingUser") followingUser :String,
        @Field("follower") follower :String,

        ) : Call<String>

    @FormUrlEncoded
    @POST("WritePostReview.php")
    fun requestWritePostReview (
        @Field("postIdx") postIdx :String,
        @Field("uploadUserEmail") uploadUserEmail :String,
        @Field("uploadUserProfile") uploadUserProfile :String,
        @Field("uploadUserNickname") uploadUserNickname :String,
        @Field("review") review :String,
        @Field("uploadDate") uploadDate :String
    ) : Call<String>

    @FormUrlEncoded
    @POST("WriteDetailDuetReview.php")
    fun requestWriteDetailDuetReview (
        @Field("detailDuetIdx") detailDuetIdx :String,
        @Field("uploadUserEmail") uploadUserEmail :String,
        @Field("uploadUserProfile") uploadUserProfile :String,
        @Field("uploadUserNickname") uploadUserNickname :String,
        @Field("review") review :String,
        @Field("uploadDate") uploadDate :String
    ) : Call<String>

    @FormUrlEncoded
    @POST("LookAtUserProfile.php")
    fun requestLookAtUserProfile (
        @Field("otherUserEmail") otherUserEmail :String,
        @Field("UserEmail") UserEmail :String,

    ) : Call<String>

    @FormUrlEncoded
    @POST("LookAtMyFollow.php")
    fun requestLookAtMyFollow (
        @Field("UserEmail") UserEmail :String

        ) : Call<String>

    @FormUrlEncoded
    @POST("deleteReplay.php")
    fun requestDeleteReplay (
        @Field("replayIdx") replayIdx :String
    ) : Call<String>

    @FormUrlEncoded
    @POST("deleteUser.php")
    fun requestDeleteUser (
        @Field("userEmail") userEmail :String
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
    @POST("editPostReview.php")
    fun requestEditPostReview (
        @Field("idx") idx :String,
        @Field("editReview") editReview :String
    ) : Call<String>

    @FormUrlEncoded
    @POST("editDetailDuetReview.php")
    fun requestEditDetailDuetReview (
        @Field("idx") idx :String,
        @Field("editReview") editReview :String
    ) : Call<String>


    @FormUrlEncoded
    @POST("deleteReplayReview.php")
    fun requestDeleteReplayReview (
        @Field("idx") idx :String,
        @Field("replayIdx") replayIdx :String
    ) : Call<String>

    @FormUrlEncoded
    @POST("deletePostReview.php")
    fun requestDeletePostReview (
        @Field("idx") idx :String,
        @Field("postIdx") postIdx :String
    ) : Call<String>

    @FormUrlEncoded
    @POST("deleteDetailDuetReview.php")
    fun requestDeleteDetailDuetReview (
        @Field("idx") idx :String,
        @Field("detailDuetIdx") detailDuetIdx :String
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
        @Field("email") email :String,
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