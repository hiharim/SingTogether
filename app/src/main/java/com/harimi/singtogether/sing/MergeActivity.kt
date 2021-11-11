package com.harimi.singtogether.sing

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.hardware.Camera
import android.icu.text.DecimalFormat
import android.media.CamcorderProfile
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.harimi.singtogether.Data.LyricsData
import com.harimi.singtogether.EarPhoneDialog
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.adapter.LyricsAdapter
import com.harimi.singtogether.databinding.ActivityMergeBinding

import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/***
 * 듀엣녹화하는 화면
 */
class MergeActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var binding: ActivityMergeBinding
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService
    private var duet_idx : Int? = null
    private var mr_idx : Int? = null
    private var title : String? = null
    private var singer : String? = null
    private var with : String? = null
    private var way : String? = null
    private var lyrics : String? = null // 가사
    private var side : String = "back"
    private lateinit var mr_path : String // 노래 mr
    private lateinit var duet_path : String // 사용자 비디오
    private lateinit var extract_path : String // 병합하고자하는 영상의 추출된 오디오
    private var mediaPlayer: MediaPlayer? = null
    private var  mRecorder : MediaRecorder?=null // 사용하지 않을 때는 메모리 해제 및 null 처리
    private var isRecording = false
    private var mPath: String=
        Environment.getExternalStorageDirectory().absolutePath + "/mergeVideo7mp4"
    var mSurfaceHolder: SurfaceHolder? = null
    var mCamera: Camera? = null

    private val mergeVideoFilePath :String by lazy {
        "${externalCacheDir?.absolutePath}/mergeVideo8.mp4"
    }

    private var file_path:String?=null
    lateinit var fileName : String // 서버로 보낼 비디오 파일 이름
    private var videoFile : File?=null // 녹화된 비디오파일
    var asyncDialog : ProgressDialog ?=null
    private lateinit var collaborationNickname : String // 병합하고자하는 유저의 닉네임
    private lateinit var collaborationEmail : String // 병합하고자하는 유저의 이메일
    private val lyricsList : ArrayList<LyricsData> = ArrayList()
    private lateinit var lyricsAdapter: LyricsAdapter
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
        binding= ActivityMergeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRetrofit()
        duet_idx=intent.getIntExtra("RECORD_DUET_IDX", 0)
        mr_idx=intent.getIntExtra("RECORD_MR_IDX", 0)
        title=intent.getStringExtra("RECORD_TITLE")
        singer=intent.getStringExtra("RECORD_SINGER")
        lyrics=intent.getStringExtra("RECORD_LYRICS")
        with=intent.getStringExtra("WITH")
        way=intent.getStringExtra("WAY")
        mr_path= intent.getStringExtra("RECORD_MR_PATH").toString()
        duet_path= intent.getStringExtra("RECORD_SONG_PATH").toString()
        extract_path= intent.getStringExtra("RECORD_EXTRACT_PATH").toString()
        collaborationNickname= intent.getStringExtra("COLLABORATION").toString()
        collaborationEmail= intent.getStringExtra("COLLABO_EMAIL").toString()

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
        binding.activityMergeRv.layoutManager= LinearLayoutManager(applicationContext)
        binding.activityMergeRv.setHasFixedSize(true)
        binding.activityMergeRv.setBackgroundColor(Color.parseColor("#81000000"))

//        mediaPlayer = MediaPlayer()
//        mediaPlayer.setDataSource(duet_path)
//        mediaPlayer.prepare()

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
                mediaPlayer?.setDataSource(duet_path)
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
                var timeFormat2 = android.icu.text.SimpleDateFormat("m.ss")  //"분:초"를 나타낼 수 있도록 포멧팅
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
                            val minusSecond=mediaPlayer!!.duration-2000
                            realBeforeTotalTime=timeFormat.format(minusSecond).toString()
                            RecordActivity.time_info.pTime= binding.activityVideo2TvPlayTime.text.toString()

                            lyricsAdapter= LyricsAdapter(lyricsList)
                            binding.activityMergeRv.adapter=lyricsAdapter
                            for(i in timeList) {
                                var minus_one=i.toFloat()-0.01.toFloat()
                                val t_down = DecimalFormat("0.00")
                                var second = t_down.format(minus_one)
                                var mTime=t_down.format(RecordActivity.time_info.pTime.toFloat())

                                Log.e("레코드액티비티", "1초 뺀 시간 second : $second")
                                Log.e("레코드액티비티", "현재 플레이시간 mTime : $mTime")
                                Log.e("레코드액티비티", "전 i : $i")
                                for(j in nextList) {
                                    var nTime = j.toFloat()
                                    var nMunusTime=j.toFloat()-0.01.toFloat()
                                    var nextTime = t_down.format(nTime)
                                    var nextMinusTime=t_down.format(nMunusTime)
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

                    // 음악이 종료되면 녹음 중지하고 AfterSingActivity 로 이동
                    if(isFinished) {
                        mediaPlayer?.stop() // 음악 정지
                        mediaPlayer?.release()

                        isRecording = false
                        mRecorder!!.stop()
                        mRecorder!!.release()
                        mRecorder = null

                        mCamera!!.release()
                        mCamera=null
                        try {
                            Thread(Runnable {
                                // ==== [UI 동작 실시] ====
                                runOnUiThread {
                                    asyncDialog = ProgressDialog(this@MergeActivity)
                                    asyncDialog!!.setProgressStyle(ProgressDialog.BUTTON_POSITIVE)
                                    asyncDialog!!.setMessage("믹싱중...")
                                    asyncDialog!!.show()
                                }
                            }).start()
                        }
                        catch(e : Exception){
                            e.printStackTrace()
                        }

                        mergeVideo()
                    }


                    // 음악이 종료되면 녹음 중지하고 AfterSingActivity 로 이동
//                    if(!mediaPlayer.isPlaying) {
//                        mediaPlayer.stop() // 음악 정지
//                        mediaPlayer.release()
//
//                        isRecording = false
//                        mRecorder!!.stop()
//                        mRecorder!!.release()
//                        mRecorder = null
//                        //mCamera!!.lock()
//                        mCamera!!.release()
//                        mCamera=null
//                        try{
//                            Thread(Runnable {
//                                // ==== [UI 동작 실시] ====
//                                runOnUiThread {
//                                    asyncDialog = ProgressDialog(this@MergeActivity)
//                                    asyncDialog!!.setProgressStyle(ProgressDialog.BUTTON_POSITIVE)
//                                    asyncDialog!!.setMessage("믹싱중...")
//                                    asyncDialog!!.show()
//                                }
//                            }).start()
//                        }
//                        catch(e : Exception){
//                            e.printStackTrace()
//                        }
//
//                        mergeVideo()
//                    }
//


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

    private fun mergeVideo() {
        val timeStamp : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        fileName = "$timeStamp.mp4"
        videoFile = File(mergeVideoFilePath)
        var requestBody : RequestBody = RequestBody.create(
            MediaType.parse("multipart/form-data"), videoFile
        )
        var body : MultipartBody.Part=
            MultipartBody.Part.createFormData("uploaded_file", fileName, requestBody)
        mergeVideoFilePath.let {
            retrofitService.requestMergeVideo(mr_path,duet_path,extract_path,body).enqueue(object : Callback<String> {
                // 통신에 성공한 경우
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    //var call_extract_path : String? = null
                    var output_path : String ?=null
                    if (response.isSuccessful) {
                        asyncDialog!!.dismiss()
                        val jsonObject = JSONObject(response.body().toString())
                        //call_extract_path= "http://3.35.236.251/"+jsonObject.getString("extract_path")
                        output_path= "http://3.35.236.251/"+jsonObject.getString("output_path")

                        // 믹싱 성공 다이얼로그
                        val builder = AlertDialog.Builder(this@MergeActivity)
                        builder.setTitle("SingTogether")
                        builder.setMessage("믹싱을 성공했습니다!")
                        builder.setPositiveButton("확인") { dialogInterface, i ->
                            val intent= Intent(applicationContext, AfterRecordActivity::class.java)
                            intent.putExtra("DUET_IDX", duet_idx)
                            intent.putExtra("MR_IDX", mr_idx)
                            intent.putExtra("FILE_PATH", output_path)
                            intent.putExtra("USER_PATH", mergeVideoFilePath)
                            intent.putExtra("WITH", with)
                            intent.putExtra("WAY", way)
                            intent.putExtra("MERGE", "Y")
                            intent.putExtra("COLLABORATION_NICKNAME", collaborationNickname)
                            intent.putExtra("COLLABO_EMAIL", collaborationEmail)
                            Log.e(
                                "머지액티비티", "idx,file_path,mergeVideoFilePath,with,way" +
                                        duet_idx + " " + file_path + " " + mergeVideoFilePath + " " + with + " " + way
                            )
                            startActivity(intent)
                            finish()
                        }
                        builder.show()

                    } else {
                        // 통신은 성공했지만 응답에 문제가 있는 경우
                        Log.e("MergeActivity", " 응답 문제" + response.code())
                        when (response.code()) {
                            404 -> fail()
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("MergeActivity", " 통신 실패" + t.message)
                }
            })

        }
    }

    // 레트로핏 초기화
    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

    fun fail(){
        Log.e("비디오2", " 통신 실패" +404)
    }

    fun initVideoRecorder() {
        mCamera = Camera.open()
        mCamera!!.setDisplayOrientation(90)
        mSurfaceHolder = binding.surfaceView.getHolder()
        mSurfaceHolder!!.addCallback(this)
        mSurfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer=null
    }

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
                mRecorder!!.setOutputFile(mergeVideoFilePath)
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
    // 녹화
//    fun startVideoRecorder() {
//        if (isRecording) {
//            mRecorder!!.stop()
//            mRecorder!!.release()
//            mRecorder = null
//            mCamera!!.lock()
//            isRecording = false
//
//        } else {
//            runOnUiThread {
//                mRecorder = MediaRecorder()
//                mCamera!!.unlock()
//                mRecorder!!.setCamera(mCamera)
//                mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
//                mRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
//                mRecorder!!.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
//                mRecorder!!.setOrientationHint(90)
//                mRecorder!!.setOutputFile(mergeVideoFilePath)
//                mRecorder!!.setPreviewDisplay(mSurfaceHolder!!.surface)
//                try {
//                    mRecorder!!.prepare()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//                mRecorder!!.start()
//                isRecording = true
//
//            }
//        }
//    }

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

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {


    }
}


