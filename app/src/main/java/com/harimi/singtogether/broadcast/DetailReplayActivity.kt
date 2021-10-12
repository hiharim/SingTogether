package com.harimi.singtogether.broadcast

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ui.PlayerControlView
import com.harimi.singtogether.Data.DetailReplayReviewData
import com.harimi.singtogether.Data.HomeData
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.adapter.DetailReplayReviewAdapter
import com.harimi.singtogether.adapter.HomeAdapter
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Field
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DetailReplayActivity : AppCompatActivity() {

    private lateinit var retrofitService: RetrofitService
    private lateinit var retrofit : Retrofit

    private var TAG :String = "DETAILREPLAY_FRAGMENT "
    private var replayIdx : String? = null
    private var thumbnail : String? = null
    private var uploadUserProfile : String? = null
    private var uploadUserNickName : String? = null
    private var uploadDate : String? = null
    private var replayTitle : String? = null
    private var replayLikeNumber : String? = null
    private var replayHits : String? = null
    private var replayReviewNumber : String? = null
    private var uploadUserEmail : String? = null
    private var replayPostLikeIdx : String? = null
    private var liked : Boolean? = null

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
        uploadDate = getintent.getStringExtra("uploadDate")
        replayTitle = getintent.getStringExtra("replayTitle")
        replayLikeNumber = getintent.getStringExtra("replayLikeNumber")
        replayHits = getintent.getStringExtra("replayHits")
        replayReviewNumber = getintent.getStringExtra("replayReviewNumber")
        uploadUserEmail = getintent.getStringExtra("uploadUserEmail")
        replayPostLikeIdx = getintent.getStringExtra("replayPostLikeIdx")
        liked = getintent.getBooleanExtra("liked",false)
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
        exoplayerReplay =findViewById(R.id.exoplayerReplay)

        rv_detailReplayReview = findViewById(R.id.rv_detailReplayReview)
        rv_detailReplayReview.layoutManager = LinearLayoutManager(this)
        rv_detailReplayReview.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        detailReplayReviewAdapter = DetailReplayReviewAdapter(detailReplayReviewDataList,this)
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
        }

        ////댓글달기
        iv_uploadReview.setOnClickListener {
            var uploadReview = et_writeReview.text.toString()
            if (uploadReview.equals("")){
                Toast.makeText(this,"댓글을 입력해주세요",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                var uploadDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                retrofit= RetrofitClient.getInstance()
                retrofitService=retrofit.create(RetrofitService::class.java)
                retrofitService.requestWriteReview(replayIdx!!,LoginActivity.user_info.loginUserEmail,LoginActivity.user_info.loginUserProfile,LoginActivity.user_info.loginUserNickname,uploadReview,uploadDate)
                    .enqueue(object : Callback<String> {
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.isSuccessful) {
                                val body = response.body().toString()
                                Log.d(TAG, body)
                                var jsonObject = JSONObject(response.body().toString())
                                var result = jsonObject.getBoolean("result")
                                if (result){
                                    et_writeReview.setText("")
                                    var getIdx = jsonObject.getString("idx")
                                    val detailReplayReviewData = DetailReplayReviewData(getIdx,LoginActivity.user_info.loginUserEmail, LoginActivity.user_info.loginUserNickname, LoginActivity.user_info.loginUserProfile, uploadReview, uploadDate, replayIdx!!)
                                    detailReplayReviewDataList.add( detailReplayReviewData)
                                    detailReplayReviewAdapter.notifyDataSetChanged()
                                    rv_detailReplayReview.scrollToPosition(detailReplayReviewDataList.size - 1)
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
            retrofitService.requestClickLike(replayIdx!!,LoginActivity.user_info.loginUserEmail,uploadDate)
                .enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            val body = response.body().toString()
                            Log.d(TAG, body)
                            var jsonObject = JSONObject(response.body().toString())
                            var result = jsonObject.getBoolean("result")
                            var idx = jsonObject.getString("idx")
                            replayPostLikeIdx = idx
                            if (result){
                                var getLikeNumber =  fragment_detail_replay_tv_like.text.toString()
                                var getLikeNumberInt =  getLikeNumber.toInt()
                                getLikeNumberInt++
                                fragment_detail_replay_tv_like.setText(getLikeNumberInt.toString())
                                iv_clickLike.visibility =View.VISIBLE
                                iv_normalLike.visibility =View.GONE
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
                                    var getLikeNumber =
                                        fragment_detail_replay_tv_like.text.toString()
                                    var getLikeNumberInt = getLikeNumber.toInt()
                                    getLikeNumberInt--
                                    fragment_detail_replay_tv_like.setText(getLikeNumberInt.toString())
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

    fun setData(){
        fragment_detail_replay_tv_title.text =replayTitle
        tv_hits.text = replayHits
        fragment_detail_replay_tv_like.text= replayLikeNumber
        fragment_detail_replay_tv_date.text = uploadDate
        tv_UploadUserNickName.text =uploadUserNickName
//        tv_reviewNumber.text = replayReviewNumber

        if (uploadUserProfile.equals("null") || uploadUserProfile.equals("")) {
            iv_uploadUserProfile.setImageResource(R.mipmap.ic_launcher_round)
        } else {
            Glide.with(this)
                .load("http://3.35.236.251/" + uploadUserProfile)
                .thumbnail(0.1f)
                .into(iv_uploadUserProfile)
        }

        detailReplayReviewLoad()
    }

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

                            val detailReplayReviewData = DetailReplayReviewData(idx,uploadUserEmail, uploadUserNickname, uploadUserProfile, review, uploadDate, replayIdx)
                            detailReplayReviewDataList.add( detailReplayReviewData)
                            detailReplayReviewAdapter.notifyDataSetChanged()
                            rv_detailReplayReview.scrollToPosition(detailReplayReviewDataList.size - 1)
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {

                }
            })
    }
}