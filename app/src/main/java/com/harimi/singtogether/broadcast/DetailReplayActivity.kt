package com.harimi.singtogether.broadcast

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.harimi.singtogether.*
import com.harimi.singtogether.Data.DetailReplayReviewData
import com.harimi.singtogether.Network.*
import com.harimi.singtogether.adapter.DetailReplayReviewAdapter
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.xml.transform.ErrorListener

class DetailReplayActivity : AppCompatActivity() {

    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit

    private var TAG :String = "DETAILREPLAY_FRAGMENT "
    private var replayIdx : String? = null
    private var thumbnail : String? = null
    private var uploadUserProfile : String? = null
    private var uploadUserNickName : String? = null
    private var getUploadDate : String? = null
    private var replayTitle : String? = null
    private var replayLikeNumber : String? = null
    private var replayHits : String? = null
    private var replayReviewNumber : String? = null
    private var uploadUserEmail : String? = null
    private var replayPostLikeIdx : String? = null
    private var replayVideo : String? = null
    private var liked : Boolean? = null
    private var uploadUserFCMToken :String ? =null
    private var getLikeNumber :String ? =null

    //실제 비디오를 플레이하는 객체의 참조 변수
    var player: ExoPlayer? = null
    //컨트롤러 뷰 참조 변수
    var exoplayerControlView: PlayerControlView? = null
    var exoPlayerView: PlayerView? = null
    var videoUri: Uri? = null

    private lateinit var fragment_detail_replay_iv_back : ImageView
    private lateinit var fragment_detail_replay_tv_title : TextView
    private lateinit var tv_hits : TextView
    private lateinit var iv_normalLike : ImageView
    private lateinit var iv_clickLike : ImageView
    private lateinit var fragment_detail_replay_tv_like : TextView
    private lateinit var fragment_detail_replay_tv_date : TextView
    private lateinit var iv_uploadUserProfile : CircleImageView
    private lateinit var tv_UploadUserNickName : TextView
//    private lateinit var tv_reviewNumber : TextView
    private lateinit var et_writeReview : EditText
    private lateinit var iv_uploadReview : ImageView
    private lateinit var exoplayerReplay : PlayerControlView
    private lateinit var iv_editMenu : ImageView


    private val detailReplayReviewDataList: ArrayList<DetailReplayReviewData> = ArrayList()
    private lateinit var rv_detailReplayReview : RecyclerView
    private lateinit var detailReplayReviewAdapter: DetailReplayReviewAdapter


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_replay)
        initView()
        setData()


    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun initView(){
        val getintent = intent
        replayIdx = getintent.getStringExtra("idx")
        uploadUserNickName = getintent.getStringExtra("uploadUserNickName")
        thumbnail = getintent.getStringExtra("thumbnail")
        uploadUserProfile = getintent.getStringExtra("uploadUserProfile")
        getUploadDate = getintent.getStringExtra("uploadDate")
        replayTitle = getintent.getStringExtra("replayTitle")
        replayLikeNumber = getintent.getStringExtra("replayLikeNumber")
        replayHits = getintent.getStringExtra("replayHits")
        replayReviewNumber = getintent.getStringExtra("replayReviewNumber")
        uploadUserEmail = getintent.getStringExtra("uploadUserEmail")
        replayPostLikeIdx = getintent.getStringExtra("replayPostLikeIdx")
        liked = getintent.getBooleanExtra("liked", false)
        replayVideo = getintent.getStringExtra("replayVideo")
        uploadUserFCMToken = getintent.getStringExtra("uploadUserFCMToken")


        getLikeNumber =replayLikeNumber

        Log.d(TAG, replayPostLikeIdx!!)
        Log.d(TAG, liked!!.toString())


        fragment_detail_replay_iv_back =findViewById(R.id.fragment_detail_replay_iv_back)
        fragment_detail_replay_tv_title =findViewById(R.id.fragment_detail_replay_tv_title)
        tv_hits =findViewById(R.id.tv_hits)
        iv_normalLike =findViewById(R.id.iv_normalLike)
        iv_clickLike =findViewById(R.id.iv_clickLike)
        fragment_detail_replay_tv_like =findViewById(R.id.fragment_detail_replay_tv_like)
        fragment_detail_replay_tv_date =findViewById(R.id.fragment_detail_replay_tv_date)
        iv_uploadUserProfile =findViewById(R.id.iv_uploadUserProfile)
        tv_UploadUserNickName =findViewById(R.id.tv_UploadUserNickName)
//        tv_reviewNumber =findViewById(R.id.tv_reviewNumber)
        et_writeReview =findViewById(R.id.et_writeReview)
        iv_uploadReview =findViewById(R.id.iv_uploadReview)
        rv_detailReplayReview =findViewById(R.id.rv_detailReplayReview)
        exoPlayerView = findViewById(R.id.exoPlayerView)
        exoplayerControlView = findViewById(R.id.exoplayerControlView)
        iv_editMenu = findViewById(R.id.iv_editMenu)


        ////프로필 화면으로 가기
        iv_uploadUserProfile.setOnClickListener {

        }


        ////내 아이디와 게시물 작성자가 맞으면 수정할수있게 보여주기
        if (LoginActivity.user_info.loginUserEmail.equals(uploadUserEmail)){
            iv_editMenu.visibility =View.VISIBLE
        }else{
            iv_editMenu.visibility =View.GONE
        }



        rv_detailReplayReview = findViewById(R.id.rv_detailReplayReview)
        rv_detailReplayReview.layoutManager = LinearLayoutManager(this)
        rv_detailReplayReview.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        detailReplayReviewAdapter = DetailReplayReviewAdapter(detailReplayReviewDataList, this)
        rv_detailReplayReview.adapter = detailReplayReviewAdapter


        if (liked ==true){
            iv_clickLike.visibility =View.VISIBLE
            iv_normalLike.visibility =View.GONE

        }else{
            iv_clickLike.visibility =View.GONE
            iv_normalLike.visibility =View.VISIBLE
        }

        ///뒤로가기
        fragment_detail_replay_iv_back.setOnClickListener {
            finish()

        val fragmentBroadcast = BraodcastFragment()
        var bundle =Bundle()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.activity_main_frame,fragmentBroadcast)
        transaction.commit()
        }

        /////수정, 삭제
        iv_editMenu.setOnClickListener {
            val popupMenu = PopupMenu(applicationContext, it)
            popupMenu.inflate(R.menu.replay_delete)
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {

                    R.id.post_delete -> {
                        val builder =
                            AlertDialog.Builder(this@DetailReplayActivity)
                        //빌더 타이틀
                        builder.setTitle("삭제")
                        //빌더 메세지
                        builder.setMessage("삭제 하시겠습니까?")
                        builder.setPositiveButton("네") {
                                dialog, which ->

                            retrofit= RetrofitClient.getInstance()
                            retrofitService=retrofit.create(RetrofitService::class.java)
                            retrofitService.requestDeleteReplay(replayIdx!!)
                                .enqueue(object : Callback<String> {
                                    override fun onResponse(call: Call<String>, response: Response<String>) {
                                        if (response.isSuccessful) {
                                            val body = response.body().toString()
                                            Log.d(TAG, body)
                                            var jsonObject = JSONObject(response.body().toString())
                                            var result = jsonObject.getBoolean("result")

                                            if (result) {
                                                val builder =
                                                    AlertDialog.Builder(this@DetailReplayActivity)
                                                //빌더 타이틀
                                                builder.setTitle("완료")
                                                //빌더 메세지
                                                builder.setMessage("삭제 되었습니다")
                                                builder.setPositiveButton("확인") {
                                                        dialog, which ->
                                                    finish()
                                                }
                                                builder.show()
                                            }
                                        }
                                    }

                                    override fun onFailure(call: Call<String>, t: Throwable) {
                                    }
                                })

                        }
                        builder.setNegativeButton("아니요") {
                                dialog, which -> dialog.dismiss()

                        }
                        builder.show()
                    }
                }
                false
            }

        }

        ////댓글달기
        iv_uploadReview.setOnClickListener {
            var uploadReview = et_writeReview.text.toString()
            if (uploadReview.equals("")){
                Toast.makeText(this, "댓글을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                var uploadDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                retrofit= RetrofitClient.getInstance()
                retrofitService=retrofit.create(RetrofitService::class.java)
                retrofitService.requestWriteReview(
                    replayIdx!!,
                    LoginActivity.user_info.loginUserEmail,
                    LoginActivity.user_info.loginUserProfile,
                    LoginActivity.user_info.loginUserNickname,
                    uploadReview,
                    uploadDate,
                    uploadUserEmail!!
                )
                    .enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.isSuccessful) {
                                val body = response.body().toString()
                                Log.d(TAG, body)
                                var jsonObject = JSONObject(response.body().toString())
                                var result = jsonObject.getBoolean("result")
                                if (result) {
                                    et_writeReview.setText("")
                                    var getIdx = jsonObject.getString("idx")
                                    val detailReplayReviewData = DetailReplayReviewData(
                                        getIdx,
                                        LoginActivity.user_info.loginUserEmail,
                                        LoginActivity.user_info.loginUserNickname,
                                        LoginActivity.user_info.loginUserProfile,
                                        uploadReview,
                                        uploadDate,
                                        replayIdx!!
                                    )
                                    detailReplayReviewDataList.add(detailReplayReviewData)
                                    detailReplayReviewAdapter.notifyDataSetChanged()
                                    rv_detailReplayReview.scrollToPosition(
                                        detailReplayReviewDataList.size - 1
                                    )
                                }

                                var isLiked = jsonObject.getBoolean("isLiked")
                                Log.d(TAG, body)
                                ////FCM 보내기
                                if (LoginActivity.user_info.loginUserEmail.equals(uploadUserEmail)){

                                }else{
                                    PushNotification(
                                        NotificationData("SingTogether", LoginActivity.user_info.loginUserNickname+" 님이 댓글을 남겼습니다.",
                                            replayIdx!!,uploadUserEmail!!,uploadUserProfile!!,uploadUserNickName!!,thumbnail!!,getUploadDate!!,replayTitle!!,replayLikeNumber!!,replayHits!!,
                                            replayReviewNumber!!,replayPostLikeIdx!!,isLiked,replayVideo!!,uploadUserFCMToken!!,"리플레이"),
                                        uploadUserFCMToken.toString()
                                    ).also {
                                        sendNotification(it)
                                    }
                                }

                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                        }
                    })
            }
        }

        //좋아요 클릭 전
        iv_normalLike.setOnClickListener {
            var uploadDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            retrofit= RetrofitClient.getInstance()
            retrofitService=retrofit.create(RetrofitService::class.java)
            retrofitService.requestClickLike(
                replayIdx!!,
                LoginActivity.user_info.loginUserEmail,
                uploadDate,
                uploadUserEmail!!
            )
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val body = response.body().toString()
                            Log.d(TAG, body)
                            var jsonObject = JSONObject(response.body().toString())
                            var result = jsonObject.getBoolean("result")
                            var idx = jsonObject.getString("idx")
                            replayPostLikeIdx = idx
                            if (result) {
//                                var getLikeNumber =  fragment_detail_replay_tv_like.text.toString()
//                                var getLikeNumberInt =  getLikeNumber.toInt()
//                                getLikeNumberInt++
                                var getLikeNumberInt = getLikeNumber!!.toInt()
                                getLikeNumberInt++

                                fragment_detail_replay_tv_like.setText(getLikeNumberInt.toString())
                                iv_clickLike.visibility = View.VISIBLE
                                iv_normalLike.visibility = View.GONE
                            }

                            var isLiked = jsonObject.getBoolean("isLiked")
                            Log.d(TAG, body)
                            ////FCM 보내기
                            if (LoginActivity.user_info.loginUserEmail.equals(uploadUserEmail)){

                            }else{
                                PushNotification(
                                    NotificationData("SingTogether", LoginActivity.user_info.loginUserNickname+" 님이 좋아요를 누르셨습니다.",
                                        replayIdx!!,uploadUserEmail!!,uploadUserProfile!!,uploadUserNickName!!,thumbnail!!,getUploadDate!!,replayTitle!!,replayLikeNumber!!,replayHits!!,
                                        replayReviewNumber!!,replayPostLikeIdx!!,isLiked,replayVideo!!,uploadUserFCMToken!!,"리플레이"),
                                    uploadUserFCMToken.toString()
                                ).also {
                                    sendNotification(it)
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                    }
                })
        }

        ///좋아요 취소
        iv_clickLike.setOnClickListener {
            if (!replayPostLikeIdx.equals("null")) {
                retrofit = RetrofitClient.getInstance()
                retrofitService = retrofit.create(RetrofitService::class.java)
                retrofitService.requestCancelLike(
                    replayIdx!!,
                    LoginActivity.user_info.loginUserEmail,
                    replayPostLikeIdx!!
                )
                    .enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.isSuccessful) {
                                val body = response.body().toString()
                                Log.d(TAG, body)
                                var jsonObject = JSONObject(response.body().toString())
                                var result = jsonObject.getBoolean("result")
                                if (result) {
//                                    var getLikeNumber =
//                                        fragment_detail_replay_tv_like.text.toString()
//                                    var getLikeNumberInt = getLikeNumber!!.toInt()
//                                    getLikeNumberInt --

                                    fragment_detail_replay_tv_like.setText(getLikeNumber.toString())
                                    iv_clickLike.visibility = View.GONE
                                    iv_normalLike.visibility = View.VISIBLE
                                }
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                        }
                    })
            }
        }
    }

    ////fcm send 메세지 && 코루틴 launch
    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d(TAG, "Response: 성공")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
    }


    //데이터 셋 해주기
    fun setData(){

        var getReplayHits =  replayHits
        var getReplayHitsInt =  getReplayHits!!.toInt()
        getReplayHitsInt++

        fragment_detail_replay_tv_title.text =replayTitle
        tv_hits.text = getReplayHitsInt.toString()
        fragment_detail_replay_tv_like.text= replayLikeNumber
        fragment_detail_replay_tv_date.text = getUploadDate
        tv_UploadUserNickName.text =uploadUserNickName
//        tv_reviewNumber.text = replayReviewNumber

        if (uploadUserProfile.equals("null") || uploadUserProfile.equals("")) {
            iv_uploadUserProfile.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            Glide.with(this)
                .load("http://3.35.236.251/" + uploadUserProfile)
//                .thumbnail(0.1f)
                .into(iv_uploadUserProfile)
        }

        detailReplayReviewLoad()
    }

    //데이터 로드
    fun detailReplayReviewLoad(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
        retrofitService.requestGetReplayReview(replayIdx!!)
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        val body = response.body().toString()
                        Log.d(TAG, body)
                        detailReplayReviewDataList.clear()

                        val jsonArray = JSONArray(body)
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)

                            val idx = jsonObject.getString("idx")
                            val uploadUserEmail = jsonObject.getString("uploadUserEmail")
                            val uploadUserProfile = jsonObject.getString("uploadUserProfile")
                            val uploadUserNickname = jsonObject.getString("uploadUserNickname")
                            val review = jsonObject.getString("review")
                            val uploadDate = jsonObject.getString("uploadDate")
                            val replayIdx = jsonObject.getString("replayIdx")


                            val detailReplayReviewData = DetailReplayReviewData(
                                idx,
                                uploadUserEmail,
                                uploadUserNickname,
                                uploadUserProfile,
                                review,
                                uploadDate,
                                replayIdx
                            )
                            detailReplayReviewDataList.add(detailReplayReviewData)
                            detailReplayReviewAdapter.notifyDataSetChanged()
                            rv_detailReplayReview.scrollToPosition(detailReplayReviewDataList.size - 1)
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {

                }
            })
    }

    override fun onStart() {
        super.onStart()
        videoUri = Uri.parse("http://3.35.236.251/" + replayVideo)
        Log.d("path ", videoUri.toString())
        //SimpleExoPlayer객체 생성
        player = SimpleExoPlayer.Builder(this.applicationContext).build()
        //플레이어뷰에게 플레이어 설정
        exoPlayerView!!.player = player
        //플레이어 컨트럴뷰와 플레이어 연동
        exoplayerControlView!!.player = player


        //비디오데이터 소스를 관리하는 DataSource 객체를 만들어주는 팩토리 객체 생성
        val factory: DataSource.Factory = DefaultDataSourceFactory(this, "Ex89VideoAndExoPlayer")
        //비디오데이터를 Uri로 부터 추출해서 DataSource객체 (CD or LP판 같은 ) 생성
        val mediaSource = ProgressiveMediaSource.Factory(factory).createMediaSource(videoUri!!)

        //만들어진 비디오데이터 소스객체인 mediaSource를
        //플레이어 객체에게 전당하여 준비하도록!![ 로딩하도록 !!]
        player!!.prepare(mediaSource)

        //로딩이 완료되어 준비가 되었을 때
        //자동 실행되도록..
        player!!.setPlayWhenReady(true);
        exoplayerControlView!!.hide()
    }

    override fun onBackPressed() {
    finish()

//        val fragmentBroadcast = BraodcastFragment()
//        var bundle =Bundle()
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.activity_main_frame,fragmentBroadcast)
//        transaction.commit()

    }

    //화면에 안보일 때..
    override fun onStop() {
        super.onStop()
        //플레이어뷰 및 플레이어 객체 초기화
        exoPlayerView!!.player = null
        player!!.release()
        player = null
    }
}