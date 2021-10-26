package com.harimi.singtogether.sing

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.harimi.singtogether.R
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.ProfileActivity
import com.harimi.singtogether.databinding.ActivityAfterSingBinding
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * 노래 녹음 다 하고 확인하고 서버에 업로드 하는 화면
 * 오디오만
 * */
class AfterSingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAfterSingBinding
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService
    private lateinit var file_path : String
    private lateinit var user_path : String // 사용자 목소리 녹음한 파일 경로
    private var nickname : String? = null
    private var with : String? = null // 솔로,듀엣 구분
    private var way : String? = null // 녹화,녹음,연습 구분
    private var mr_idx : Int? = null // mr 인덱스 값
    private var duet_idx : Int? = null // duet 인덱스 값
    private var collaborationNickname: String? = null // 듀엣한 사람 닉네임
    private var collaborationEmail: String? = null // 듀엣한 사람 닉네임
    private var isMerge : String? = null // 병합,그냥녹화 구분
    private var simpleExoPlayer: ExoPlayer?=null
    lateinit var mediaPlayer: MediaPlayer
    private var audioFile : File?=null // 녹음된 사용자 목소리 오디오 파일
    lateinit var fileName : String // 서버로 보낼 사용자 목소리 + MR 파일 이름
    private var videoUri : Uri? = null // video 저장될 Uri
    private var uri : Uri?=null
    private var circle_profile : String? = null
    var asyncDialog : ProgressDialog?=null

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAfterSingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRetrofit()

        file_path=intent.getStringExtra("FILE_PATH").toString()
        user_path=intent.getStringExtra("USER_PATH").toString()
        with=intent.getStringExtra("WITH")
        way=intent.getStringExtra("WAY")
        mr_idx=intent.getIntExtra("MR_IDX",0)
        duet_idx=intent.getIntExtra("DUET_IDX",0)
        circle_profile=intent.getStringExtra("CIRCLE_PROFILE")
        isMerge=intent.getStringExtra("MERGE")
        collaborationNickname=intent.getStringExtra("COLLABORATION_NICKNAME")
        collaborationEmail=intent.getStringExtra("COLLABO_EMAIL")
        Log.e("애프터싱액티비티", "mr_idx" + mr_idx)
        Log.e("애프터싱액티비티","mr_idx,duet_idx,file_path,user_path,with,way"+
                mr_idx+" "+duet_idx+" "+" "+file_path+" "+user_path+" "+with+" "+way)

        // 빌드 시 context 가 필요하기 때문에 context 를 null 체크 해준 뒤 빌드
        applicationContext?.let{
            simpleExoPlayer=SimpleExoPlayer.Builder(it).build()
        }
        binding.exoPlayerView.player = simpleExoPlayer
        val factory: DataSource.Factory = DefaultDataSourceFactory(
            applicationContext,
            "ExoPlayer"
        )
        val mediaItem = MediaItem.fromUri(Uri.parse(file_path))
        val progressiveMediaSource = ProgressiveMediaSource.Factory(factory)
            .createMediaSource(mediaItem)

        simpleExoPlayer!!.setMediaSource(progressiveMediaSource)
        simpleExoPlayer!!.prepare()
        Glide.with(this).load(circle_profile).into(binding.imageViewThumb)
        binding.imageViewThumb.visibility=View.VISIBLE

        simpleExoPlayer!!.play()
        // 업로드 버튼 클릭
        binding.activityAfterSingBtnUpload.setOnClickListener {
            asyncDialog = ProgressDialog(this@AfterSingActivity)
            asyncDialog!!.setProgressStyle(ProgressDialog.BUTTON_POSITIVE)
            asyncDialog!!.setMessage("업로드중...")
            asyncDialog!!.show()

            if(isMerge.equals("Y")) {
                uploadMergeAudio()
            }else if(isMerge.equals("N")){
                uploadAudio()
            }

        }

    }

    private fun uploadMergeAudio() {
        val email= LoginActivity.user_info.loginUserEmail
        val nickname= LoginActivity.user_info.loginUserNickname
        val kinds="녹음"
        mr_idx?.let {
            duet_idx?.let { it1 ->
                circle_profile?.let { it2 ->
                    retrofitService.requestUploadMergeAudio(it,
                        duet_idx!!,
                        it2,file_path,email,nickname,collaborationNickname!!,collaborationEmail!!,kinds).enqueue(object : Callback<String> {
                        // 통신에 성공한 경우
                        override fun onResponse(call: Call<String>, response: Response<String>) {
                            if (response.isSuccessful) {
                                // 응답을 잘 받은 경우
                                asyncDialog!!.dismiss()
                                Log.e("AfterSingActivity", " 통신 성공:"+ response.body().toString())
                                // 업로드 성공 다이얼로그
                                val builder = AlertDialog.Builder(this@AfterSingActivity)
                                builder.setTitle("SingTogether")
                                builder.setMessage("업로드를 성공했습니다!")
                                builder.setPositiveButton("확인") { dialogInterface, i ->
                                    simpleExoPlayer?.release()
                                    finish() }
                                builder.show()

                            } else {
                                // 통신은 성공했지만 응답에 문제가 있는 경우
                                Log.e("AfterRecordActivity", " 응답 문제" + response.code())
                                Log.e("AfterRecordActivity", " 응답 문제" + response.errorBody().toString())
                            }
                        }

                        override fun onFailure(call: Call<String>, t: Throwable) {
                            Log.e("AfterRecordActivity", " 통신 실패" + t.message)
                        }


                    })
                }
            }
        }
    }

    private fun uploadAudio() {
        val nickname= LoginActivity.user_info.loginUserNickname
        val email= LoginActivity.user_info.loginUserEmail
        val kinds="녹음"
        mr_idx?.let {
            circle_profile?.let { it1 ->
                retrofitService.requestUploadAudio(it, it1,file_path,user_path,nickname,email,kinds,with!!).enqueue(object : Callback<String> {
                    // 통신에 성공한 경우
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            // 응답을 잘 받은 경우
                            asyncDialog!!.dismiss()
                            Log.e("AfterSingActivity", " 통신 성공:"+ response.body().toString())
                            // 업로드 성공 다이얼로그
                            val builder = AlertDialog.Builder(this@AfterSingActivity)
                            builder.setTitle("SingTogether")
                            builder.setMessage("업로드를 성공했습니다!")
                            builder.setPositiveButton("확인") { dialogInterface, i ->
                                simpleExoPlayer?.release()
                                finish() }
                            builder.show()

                        } else {
                            // 통신은 성공했지만 응답에 문제가 있는 경우
                            Log.e("AfterRecordActivity", " 응답 문제" + response.code())
                            Log.e("AfterRecordActivity", " 응답 문제" + response.errorBody().toString())
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.e("AfterRecordActivity", " 통신 실패" + t.message)
                    }


                })
            }
        }
    }

//    private fun upload() {
//        //audioFile=File("${externalCacheDir?.absolutePath}/newUserMrAudio.m4a")
//        audioFile=File(user_path)
//        val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
//        // Create an image file name
//        fileName = "$timeStamp.m4a"
//        var requestBody : RequestBody = RequestBody.create(
//            MediaType.parse("multipart/form-data"), audioFile)
//        var body : MultipartBody.Part=
//            MultipartBody.Part.createFormData("uploaded_file", fileName, requestBody)
//        idx?.let {
//            nickname?.let { it1 ->
//                retrofitService.requestUpload(it, it1,body).enqueue(object : Callback<String> {
//                    // 통신에 성공한 경우
//                    override fun onResponse(call: Call<String>, response: Response<String>) {
//                        if (response.isSuccessful) {
//                            // 응답을 잘 받은 경우
//                            Log.e("비디오", " 통신 성공: ${response.body().toString()}")
//                            // 업로드 성공 다이얼로그
//                            val builder = AlertDialog.Builder(this@AfterSingActivity)
//                            builder.setTitle("SingTogether")
//                            builder.setMessage("업로드를 성공했습니다!")
//                            builder.setPositiveButton("확인") { dialogInterface, i ->
//                                simpleExoPlayer?.release()
//                                finish() }
//                            builder.show()
//
//                        } else {
//                            // 통신은 성공했지만 응답에 문제가 있는 경우
//                            Log.e("비디오", " 응답 문제" + response.code())
//                        }
//                    }
//
//                    override fun onFailure(call: Call<String>, t: Throwable) {
//                        Log.e("비디오", " 통신 실패" + t.message)
//                    }
//                })
//            }
//        }
//    }



    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }
}


