package com.harimi.singtogether.sing

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.hardware.Camera
import android.icu.text.DecimalFormat
import android.media.CamcorderProfile
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.harimi.singtogether.Data.LyricsData
import com.harimi.singtogether.EarPhoneDialog
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.adapter.LyricsAdapter
import com.harimi.singtogether.databinding.ActivityVideo2Binding
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 녹화하는 액티비티
 * FFMPEG - MR + 사용자 오디오 merge
 * */
class Video2Activity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var binding: ActivityVideo2Binding
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService
    lateinit var fileName : String // 서버로 보낼 비디오 파일 이름
    private var videoFile : File?=null // 녹화된 비디오파일
    private var nickname : String? = null
    private var idx : Int? = null
    private var title : String? = null
    private var singer : String? = null
    private var with : String? = null
    private var way : String? = null
    private var lyrics : String? = null // 가사
    private var side : String = "back" // 전면카메라,후면카메라 구분 front:전, back :후
    private lateinit var song_path : String // 노래 mr
    private var mediaPlayer: MediaPlayer? = null
    private var  mRecorder : MediaRecorder?=null // 사용하지 않을 때는 메모리 해제 및 null 처리
    private var isRecording = false
    private var mPath: String? = null
    var mSurfaceHolder: SurfaceHolder? = null
    var mCamera: Camera? = null

    private val recordingVideoFilePath :String by lazy {
        "${externalCacheDir?.absolutePath}/recordVideos28.mp4"
    }

    var asyncDialog : ProgressDialog?=null
    private var file_path:String?=null
    private val lyricsList : ArrayList<LyricsData> = ArrayList()
    private lateinit var lyricsAdapter: LyricsAdapter
    var time :String?=null
    private val timeList:ArrayList<String> = ArrayList()
    private val nextList:ArrayList<String> = ArrayList()
    private var pausePosition : Int ?=null
    private var finishPosition : Int ?=null
    private var isFinished=false
    private var isPaused=false
    private lateinit var beforeTotalTime : String
    private var realBeforeTotalTime : String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityVideo2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        initRetrofit()
        idx=intent.getIntExtra("RECORD_IDX", 0)
        title=intent.getStringExtra("RECORD_TITLE")
        singer=intent.getStringExtra("RECORD_SINGER")
        lyrics=intent.getStringExtra("RECORD_LYRICS")
        with=intent.getStringExtra("WITH")
        way=intent.getStringExtra("WAY")
        song_path= intent.getStringExtra("RECORD_SONG_PATH").toString()

        // 툴바 색깔 지정
        binding.toolbarRecord.setBackgroundColor(resources.getColor(R.color.dark_purple))

        // 제목
        binding.activityRecordTvTitle.text=title
        // 가수
        binding.activityRecordTvSinger.text=singer
        // 가사
        val array = lyrics?.split(" ★".toRegex())?.toTypedArray()
        if (array != null) {
            for (i in array.indices) {
                println(array[i])
                val seconds=array[i]
                val line= array[i].substring(9)
                val lyricsData=LyricsData(seconds, line)
                lyricsList.add(lyricsData)

                val times=array[i].substring(1,5)
                Log.e("레코드액티비티","times"+times)
                timeList.add(times)

                val next=array[i].substring(1,5)
                nextList.add(next)

            }
        }
        nextList.removeAt(0)

        //리사이클러뷰 설정
        binding.activityVideo2Rv.layoutManager= LinearLayoutManager(applicationContext)
        binding.activityVideo2Rv.setHasFixedSize(true)
        binding.activityVideo2Rv.setBackgroundColor(Color.parseColor("#81000000"))

//        mediaPlayer = MediaPlayer()
//        mediaPlayer?.setDataSource(song_path)
//        mediaPlayer?.prepare()

        initVideoRecorder()

        val dialog = EarPhoneDialog(this)
        dialog.myDig()


        // 일시정지버튼 클릭
        binding.activityRecordBtnPause.setOnClickListener {
            // 재생중이면 일시정지
            if (!isPaused) {
                binding.activityRecordBtnStart.visibility = View.VISIBLE
                binding.activityRecordBtnPause.visibility = View.GONE
                mediaPlayer?.pause()
                pausePosition=mediaPlayer?.currentPosition
                mRecorder!!.pause()
                isPaused=true
            }
        }


        // 닫기 버튼 클릭
        binding.fragmentVideo2IBtnClose.setOnClickListener {
            if (!isPaused) {
                binding.activityRecordBtnStart.visibility = View.VISIBLE
                binding.activityRecordBtnPause.visibility = View.GONE
                mediaPlayer?.pause()
                pausePosition=mediaPlayer?.currentPosition
                mRecorder!!.pause()
                isPaused=true
            }
            val builder = AlertDialog.Builder(this)
            builder.setTitle("녹화를 종료하시겠습니까? ")
            builder.setMessage("지금 녹화를 종료하시면 저장되지 않습니다.")
            builder.setPositiveButton("네") { dialog, which ->
                mRecorder?.release()
                mRecorder=null
                finish()
            }
            builder.setNegativeButton("아니오") { dialog, which ->
                // 노래 이어부르기
            }
            builder.show()
        }

        // 마이크 버튼 클릭
        binding.activityRecordBtnStart.setOnClickListener {
            // 노래 재생

            binding.activityRecordBtnStart.visibility= View.GONE
            binding.activityRecordBtnPause.visibility= View.VISIBLE

            //카메라 전환버튼 숨기기
            binding.btnConvert.visibility=View.GONE

            //startVideoRecorder()
            if(!isPaused){
                mediaPlayer = MediaPlayer()
                mediaPlayer?.setDataSource(song_path)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                startVideoRecorder()
                isFinished=false
            }else{
                // resume
                binding.activityRecordBtnStart.visibility = View.GONE
                binding.activityRecordBtnPause.visibility = View.VISIBLE
                mediaPlayer?.seekTo(pausePosition!!)
                mediaPlayer?.start()
                mRecorder!!.resume()
                isPaused=false
            }

            /* 실시간으로 변경되는 진행시간과 시크바를 구현하기 위한 스레드 사용*/
            object : Thread() {

                var timeFormat2 = android.icu.text.SimpleDateFormat("m.ss")  // 가사스레드위해서
                var timeFormat = android.icu.text.SimpleDateFormat("mm:ss")  //"분:초"를 나타낼 수 있도록 포멧팅

                override fun run() {
                    super.run()
                    if (mediaPlayer == null)
                        return
                    binding.seekBar.max = mediaPlayer!!.duration  // mPlayer.duration : 음악 총 시간
                    finishPosition=mediaPlayer!!.duration
                    while (mediaPlayer!!.isPlaying) {
                        runOnUiThread { //화면의 위젯을 변경할 때 사용 (이 메소드 없이 아래 코드를 추가하면 실행x)
                            binding.seekBar.progress = mediaPlayer!!.currentPosition
                            binding.activityRecordTvIngTime.text = timeFormat.format(mediaPlayer!!.currentPosition)
                            binding.activityRecordTvTotalTime.text=timeFormat.format(mediaPlayer!!.duration)
                            binding.activityVideo2TvPlayTime.text=timeFormat2.format(mediaPlayer!!.currentPosition)

                            beforeTotalTime=binding.activityRecordTvTotalTime.text.toString()
                            Log.e("beforeTotalTime",": $beforeTotalTime")

                            val minusSecond=mediaPlayer!!.duration-1000
                            realBeforeTotalTime=timeFormat.format(minusSecond).toString()
                            Log.e("realBeforeTotalTime",": ${realBeforeTotalTime}")

                            RecordActivity.time_info.pTime= binding.activityVideo2TvPlayTime.text.toString()

                            lyricsAdapter= LyricsAdapter(lyricsList)
                            binding.activityVideo2Rv.adapter=lyricsAdapter
                            for(i in timeList) {
                                var minus_one=i.toFloat()-0.01.toFloat()
                                val t_down = DecimalFormat("0.00")
                                var second = t_down.format(minus_one)
                                var mTime=t_down.format(RecordActivity.time_info.pTime.toFloat())

                                Log.e("레코드액티비티", "1초 뺀 시간 second : $second")
                                Log.e("레코드액티비티", "현재 플레이시간 mTime : $mTime")
                                Log.e("레코드액티비티", "전 i : $i")

                                for(j in nextList) {
                                    val nTime = j.toFloat()
                                    val nMunusTime=j.toFloat()-0.01.toFloat()
                                    val nextTime = t_down.format(nTime)
                                    val nextMinusTime=t_down.format(nMunusTime)
                                    Log.e("레코드액티비티", "nextTime  : $nextTime ")
                                    Log.e("레코드액티비티", "j : $j")

                                    if (mTime.toString() == nextTime.toString() && second==nextMinusTime) {
                                        lyricsList.removeAt(0)
                                        lyricsAdapter.notifyItemRemoved(0)
                                    }

                                }
                            }
                        }

                        SystemClock.sleep(1000)
                    }
                    if(binding.activityRecordTvIngTime.text.equals(realBeforeTotalTime)){
                        isFinished=true
                    }

                    if(isFinished) {
                        mediaPlayer?.stop() // 음악 정지
                        mediaPlayer?.release()

                        isRecording = false
                        mRecorder!!.stop()
                        mRecorder!!.release()
                        mRecorder = null

                        mCamera!!.release()
                        mCamera=null

                        try{
                            Thread(Runnable {
                                // ==== [UI 동작 실시] ====
                                runOnUiThread {
                                    asyncDialog = ProgressDialog(this@Video2Activity)
                                    asyncDialog!!.setProgressStyle(ProgressDialog.BUTTON_POSITIVE)
                                    asyncDialog!!.setMessage("믹싱중...")
                                    asyncDialog!!.show()
                                }
                            }).start()
                        }
                        catch (e: Exception){
                            e.printStackTrace()
                        }
                        mixVideo()
                    }

                    // 음악이 종료되면 녹음 중지하고 AfterSingActivity 로 이동
//                    if(!mediaPlayer!!.isPlaying) {
//                        mediaPlayer?.stop() // 음악 정지
//                        mediaPlayer?.release()
//
//                        isRecording = false
//                        mRecorder!!.stop()
//                        mRecorder!!.release()
//                        mRecorder = null
//                        //mCamera!!.lock()
//                        mCamera!!.release()
//                        mCamera=null
//
//                        try{
//                            Thread(Runnable {
//                                // ==== [UI 동작 실시] ====
//                                runOnUiThread {
//                                    asyncDialog = ProgressDialog(this@Video2Activity)
//                                    asyncDialog!!.setProgressStyle(ProgressDialog.BUTTON_POSITIVE)
//                                    asyncDialog!!.setMessage("믹싱중...")
//                                    asyncDialog!!.show()
//                                }
//                            }).start()
//                        }
//                        catch (e: Exception){
//                            e.printStackTrace()
//                        }
//                        mixVideo()
//                    }

                }
            }.start()

            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaPlayer?.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })

        }

        //카메라 전환
       binding.btnConvert.setOnClickListener {
           //switchCamera()
           changeCamera()

        }

    }

    // 뒤로가기 버튼
    override fun onBackPressed() {
        super.onBackPressed()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("녹화를 종료하시겠습니까? ")
        builder.setMessage("지금 녹화를 종료하시면 저장되지 않습니다.")
        builder.setPositiveButton("네") { dialog, which ->
            mediaPlayer?.release()
            mediaPlayer=null
        }
        builder.setNegativeButton("아니오") { dialog, which ->
            // 노래 이어부르기
        }
        builder.show()
    }

    // 앱이 백그라운드로 넘어가도 음악이 계속 실행되는거 막기 위해서 오버라이드
    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer=null
    }

    fun initVideoRecorder() {
        mCamera = Camera.open()
        mCamera!!.setDisplayOrientation(90)
        mSurfaceHolder = binding.surfaceView.getHolder()
        mSurfaceHolder!!.addCallback(this)
        mSurfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        mCamera!!.lock()
//    }


    fun startVideoRecorder() {
        if (isFinished) {
            mRecorder!!.stop()
            mRecorder!!.release()
            mRecorder = null
            mCamera!!.lock()
            isRecording = false
            //isFinished=false

        } else {
            runOnUiThread {
                mRecorder = MediaRecorder()
                mCamera!!.unlock()
                mRecorder!!.setCamera(mCamera)
                mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                mRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
                if(side.equals("front")){
                    mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    mRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                    mRecorder!!.setOrientationHint(270) // 전면 좌우반전
                }else{
                    // 후면일땐 QUALITY_HIGH , 전면일땐 설정..
                    side="back"
                    mRecorder!!.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
                    mRecorder!!.setOrientationHint(90) // 후면
                }
                mRecorder!!.setOutputFile(recordingVideoFilePath)
                mRecorder!!.setPreviewDisplay(mSurfaceHolder!!.surface)
                try {
                    mRecorder!!.prepare()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mRecorder!!.start()
                isRecording = true
                isFinished=false

            }
        }
    }

    // 레트로핏 초기화
    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

   // 서버에서 mix
   private fun mixVideo() {
       val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
       fileName = "$timeStamp.mp4"
       nickname= LoginActivity.user_info.loginUserNickname
       videoFile = File(recordingVideoFilePath)
       Log.e("mixVideo()", "side :" + side)
       var requestBody : RequestBody = RequestBody.create(
           MediaType.parse("multipart/form-data"), videoFile
       )
       var body : MultipartBody.Part=
           MultipartBody.Part.createFormData("uploaded_file", fileName, requestBody)
       song_path.let {
           retrofitService.requestMixVideo(it, side, body).enqueue(object : Callback<String> {
               // 통신에 성공한 경우
               override fun onResponse(call: Call<String>, response: Response<String>) {
                   var extract_path: String? = null
                   var finish_path: String? = null
                   if (response.isSuccessful) {
                       // 응답을 잘 받은 경우
                       asyncDialog!!.dismiss()
                       val jsonObject = JSONObject(response.body().toString())
                       extract_path = "http://3.35.236.251/" + jsonObject.getString("extract_path")
                       finish_path = "http://3.35.236.251/" + jsonObject.getString("finish_path")

                       // 믹싱 성공 다이얼로그
                       val builder = AlertDialog.Builder(this@Video2Activity)
                       builder.setTitle("SingTogether")
                       builder.setMessage("믹싱을 성공했습니다!")
                       builder.setCancelable(false)
                       builder.setPositiveButton("확인") { dialogInterface, i ->
                           val intent = Intent(applicationContext, AfterRecordActivity::class.java)
                           intent.putExtra("MR_IDX", idx)
                           intent.putExtra("FILE_PATH", finish_path) // 최종완성된 비디오 파일 path
                           intent.putExtra("USER_PATH", extract_path) // 추출한 오디오 파일 path
                           intent.putExtra("WITH", with)
                           intent.putExtra("WAY", way)
                           intent.putExtra("MERGE", "N")
                           intent.putExtra("COLLABORATION_NICKNAME", "NULL")
                           intent.putExtra("COLLABO_EMAIL", "NULL")
                           Log.e(
                               "비디오액티비티", "idx,file_path,recordingVideoFilePath,with,way" +
                                       idx + " " + file_path + " " + recordingVideoFilePath + " " + with + " " + way
                           )
                           startActivity(intent)
                           finish()
                       }
                       builder.show()

                   } else {
                       // 통신은 성공했지만 응답에 문제가 있는 경우
                       Log.e("비디오2", " 응답 문제" + response.code())
                       when (response.code()) {
                           404 -> fail()
                       }
                   }
               }

               override fun onFailure(call: Call<String>, t: Throwable) {
                   Log.e("비디오2", " 통신 실패" + t.message)
               }
           })

       }
   }

    fun fail(){
        Log.e("비디오2", " 통신 실패" + 404)
    }

    class time_info{
        companion object {
            var pTime = ""
        }
    }

    fun changeCamera(){
        if (Camera.getNumberOfCameras() >= 2) {
            mCamera!!.stopPreview()
        }
        mCamera!!.release()
        var which=0
        when (which) {
            0 -> {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
                which = 1
                side = "front"
            }
            1 -> {
                mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
                which = 0
                //side="back"
            }
        }

        //setCameraDisplayOrientation(this@Video2Activity, currentCameraId, mCamera)
        mCamera!!.setDisplayOrientation(90)
        try {
            mCamera!!.setPreviewDisplay(mSurfaceHolder)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mCamera!!.startPreview()
    }

    fun switchCamera() {
        //myCamera is the Camera object
        if (Camera.getNumberOfCameras() >= 2) {
            mCamera?.stopPreview()
            mCamera?.release()

            var which=0
            when (which) {
                0 -> {
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
                    which = 1
                }
                1 -> {
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
                    which = 0
                }
            }
            try {
                mCamera?.setPreviewDisplay(mSurfaceHolder)
                mCamera!!.setDisplayOrientation(90)
                //"this" is a SurfaceView which implements SurfaceHolder.Callback,
                //as found in the code examples
                //mCamera?.setPreviewCallback(this@Video2Activity)
                mCamera?.startPreview()
            } catch (exception: IOException) {
                mCamera?.release()
                mCamera = null
            }
        }
    }

    // surfaceView 가 생성될 때
    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    // surfaceView 가 바뀔때
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {


    }





}