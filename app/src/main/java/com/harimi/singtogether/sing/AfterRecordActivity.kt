package com.harimi.singtogether.sing

import android.app.ProgressDialog
import android.media.MediaPlayer
import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.harimi.singtogether.Data.MRData
import com.harimi.singtogether.Data.MySongData
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.databinding.ActivityAfterRecordBinding
import com.harimi.singtogether.databinding.ActivityAfterSingBinding
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 노래 녹화 다 하고 확인하고 서버에 업로드 하는 화면
 * 비디오+오디오 둘다 업로드
 * */
class AfterRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAfterRecordBinding
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService
    private lateinit var file_path : String
    private lateinit var user_path : String // 사용자 목소리 녹음한 파일 경로
    private var collaborationNickname: String? = null // 듀엣한 사람 닉네임
    private lateinit var nickname : String
    private var isMerge : String? = null // 병합,그냥녹화 구분
    private var with : String? = null // 솔로,듀엣 구분
    private var way : String? = null // 녹화,녹음,연습 구분
    private var idx : Int? = null // mr 인덱스 값
    private var simpleExoPlayer: ExoPlayer?=null
    lateinit var mediaPlayer: MediaPlayer
    private var audioFile : File?=null // 녹음된 사용자 목소리 오디오 파일
    lateinit var fileName : String // 서버로 보낼 사용자 목소리 + MR 파일 이름
    var asyncDialog : ProgressDialog ?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityAfterRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRetrofit()

        file_path=intent.getStringExtra("FILE_PATH").toString() // 최종완성된 비디오 path
        user_path=intent.getStringExtra("USER_PATH").toString() // 추출한 오디오 path
        with=intent.getStringExtra("WITH")
        way=intent.getStringExtra("WAY")
        isMerge=intent.getStringExtra("MERGE")
        collaborationNickname=intent.getStringExtra("COLLABORATION_NICKNAME")
        idx=intent.getIntExtra("MR_IDX",0)

        Log.e("애프터싱액티비티","idx,file_path,user_path,with,way"+
                idx+" "+file_path+" "+user_path+" "+with+" "+way)
        nickname= LoginActivity.user_info.loginUserNickname

        // 빌드 시 context 가 필요하기 때문에 context 를 null 체크 해준 뒤 빌드
        applicationContext?.let{
            simpleExoPlayer= SimpleExoPlayer.Builder(it).build()
        }
        binding.exoPlayerView.player = simpleExoPlayer
        val factory: DataSource.Factory = DefaultDataSourceFactory(
            applicationContext,
            "ExoPlayer"
        )

        var mediaItem = MediaItem.fromUri(Uri.parse(file_path))
        //val mediaItem = MediaItem.fromUri(Uri.parse(user_path))

        if(with.equals("솔로")){
            mediaItem=MediaItem.fromUri(Uri.parse(user_path))
        }else if(with.equals("듀엣")){
            mediaItem=MediaItem.fromUri(Uri.parse(file_path))
        }

        val progressiveMediaSource = ProgressiveMediaSource.Factory(factory)
            .createMediaSource(mediaItem)

        simpleExoPlayer!!.setMediaSource(progressiveMediaSource)
        simpleExoPlayer!!.prepare()
        simpleExoPlayer!!.play()
        // 업로드 버튼 클릭
        binding.activityAfterRecordBtnUpload.setOnClickListener {
            asyncDialog  = ProgressDialog(this@AfterRecordActivity)
            /* UI Thread: 프로그래스 바 등 준비 */
            asyncDialog!!.setProgressStyle(ProgressDialog.BUTTON_POSITIVE)
            asyncDialog!!.setMessage("업로드 중...")
            asyncDialog!!.show()

            if(isMerge.equals("Y")){
                uploadMergeVideo()
            }else if(isMerge.equals("N")){
                uploadVideo()
            }

        }
    }

    private fun uploadMergeVideo() {
        idx?.let {
            collaborationNickname?.let { it1 ->
                retrofitService.requestUploadMergeVideo(it,file_path,nickname, it1).enqueue(object : Callback<String> {
                    // 통신에 성공한 경우
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            // 응답을 잘 받은 경우
                            asyncDialog!!.dismiss()


                            Log.e("AfterRecordActivity", " 통신 성공:"+ response.body().toString())
                            // 업로드 성공 다이얼로그
                            val builder = AlertDialog.Builder(this@AfterRecordActivity)
                            builder.setTitle("SingTogether")
                            builder.setMessage("업로드를 성공했습니다!")
                            builder.setPositiveButton("확인") { dialogInterface, i ->
                                simpleExoPlayer?.release()
                                finish()
                            }
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



    private fun uploadVideo() {
        val nickname=LoginActivity.user_info.loginUserNickname
        idx?.let {
            retrofitService.requestUploadVideo(it,file_path,user_path,nickname).enqueue(object : Callback<String> {
                // 통신에 성공한 경우
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        // 응답을 잘 받은 경우
                        asyncDialog!!.dismiss()
                        Log.e("AfterRecordActivity", " 통신 성공:"+ response.body().toString())
                        // 업로드 성공 다이얼로그
                        val builder = AlertDialog.Builder(this@AfterRecordActivity)
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





    // 사용자 비디오 업로드
    private fun upload() {
        //audioFile=File("${externalCacheDir?.absolutePath}/newUserMrAudio.m4a")
        //audioFile=File(file_path)
        if(with.equals("솔로")){
            audioFile=File(user_path)
        }else if(with.equals("듀엣")){
            audioFile=File(file_path)
        }
        val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        // Create an image file name
        //fileName = "$timeStamp.m4a"
        fileName = "$timeStamp.mp4"
        var requestBody : RequestBody = RequestBody.create(
            MediaType.parse("multipart/form-data"), audioFile)
        var body : MultipartBody.Part=
            MultipartBody.Part.createFormData("uploaded_file", fileName, requestBody)
        idx?.let {
            nickname?.let { it1 ->
                retrofitService.requestUpload(it, it1,body).enqueue(object : Callback<String> {
                    // 통신에 성공한 경우
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            // 응답을 잘 받은 경우
                            asyncDialog!!.dismiss()
                            Log.e("비디오", " 통신 성공: ${response.body().toString()}")
                            // 업로드 성공 다이얼로그
                            val builder = AlertDialog.Builder(this@AfterRecordActivity)
                            builder.setTitle("SingTogether")
                            builder.setMessage("업로드를 성공했습니다!")
                            builder.setPositiveButton("확인") { dialogInterface, i ->
                                simpleExoPlayer?.release()
                                finish() }
                            builder.show()

                        } else {
                            // 통신은 성공했지만 응답에 문제가 있는 경우
                            Log.e("비디오", " 응답 문제" + response.code())
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.e("비디오", " 통신 실패" + t.message)
                    }
                })
            }
        }
    }

    // 레트로핏 초기화
    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }
}