package com.harimi.singtogether.sing

import android.app.ProgressDialog
import android.content.Intent
import android.hardware.Camera
import android.hardware.camera2.CameraDevice
import android.media.CamcorderProfile
import android.media.ExifInterface
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.util.SparseIntArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFmpegExecution
import com.harimi.singtogether.Data.MRData
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivityVideo2Binding
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

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
    private lateinit var song_path : String // 노래 mr
    lateinit var mediaPlayer: MediaPlayer
    private var  mRecorder : MediaRecorder?=null // 사용하지 않을 때는 메모리 해제 및 null 처리
    private var isRecording = false
    private var mPath: String? = null
    var mSurfaceHolder: SurfaceHolder? = null
    var mCamera: Camera? = null

    private val recordingVideoFilePath :String by lazy {
        "${externalCacheDir?.absolutePath}/recordVideos28.mp4"
    }
    private val outputVideoFilePath :String by lazy {
        "${externalCacheDir?.absolutePath}/outputVideo28.mp4"
    }
    private val lastVideoFilePath :String by lazy {
        "${externalCacheDir?.absolutePath}/lastVideo28.mp4"
    }
    private val extractFilePath :String by lazy {
        "${externalCacheDir?.absolutePath}/extractVideo28.m4a"
    }

    var asyncDialog : ProgressDialog?=null

    private var file_path:String?=null
    private lateinit var mCameraDevice: CameraDevice
    var mCameraId = CAMERA_BACK

    companion object
    {
        const val CAMERA_BACK = "0"
        const val CAMERA_FRONT = "1"

        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(ExifInterface.ORIENTATION_NORMAL, 0)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_90, 90)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_180, 180)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_270, 270)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityVideo2Binding.inflate(layoutInflater)
        setContentView(binding.root)

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
        var result=lyrics?.replace(" ★", "\n")
        binding.activityRecordTvLyrics.text= result.toString()

        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(song_path)
        mediaPlayer.prepare()

        initVideoRecorder()
        // 마이크 버튼 클릭
        binding.activityRecordBtnStart.setOnClickListener {
            // 노래 재생
            mediaPlayer.start()

            startVideoRecorder()

            /* 실시간으로 변경되는 진행시간과 시크바를 구현하기 위한 스레드 사용*/
            object : Thread() {
                var timeFormat = SimpleDateFormat("mm:ss")  //"분:초"를 나타낼 수 있도록 포멧팅
                override fun run() {
                    super.run()
                    if (mediaPlayer == null)
                        return
                    binding.seekBar.max = mediaPlayer.duration  // mPlayer.duration : 음악 총 시간

                    while (mediaPlayer.isPlaying) {
                        runOnUiThread { //화면의 위젯을 변경할 때 사용 (이 메소드 없이 아래 코드를 추가하면 실행x)
                            binding.seekBar.progress = mediaPlayer.currentPosition
                            binding.activityRecordTvIngTime.text = timeFormat.format(mediaPlayer.currentPosition)
                            binding.activityRecordTvTotalTime.text=timeFormat.format(mediaPlayer.duration)
                        }
                        SystemClock.sleep(200)
                    }

                    // 음악이 종료되면 녹음 중지하고 AfterSingActivity 로 이동
                    if(!mediaPlayer.isPlaying) {
                        mediaPlayer.stop() // 음악 정지
                        mediaPlayer.release()

                        mRecorder!!.stop()
                        mRecorder!!.release()
                        mRecorder = null
                        mCamera!!.lock()
                        isRecording = false

                        //videoMerge2()


                        mixVideo()

//                        val intent= Intent(applicationContext, AfterRecordActivity::class.java)
//                        intent.putExtra("MR_IDX", idx)
//                        intent.putExtra("FILE_PATH", file_path)
//                        intent.putExtra("USER_PATH", recordingVideoFilePath)
//                        intent.putExtra("WITH", with)
//                        intent.putExtra("WAY", way)
//                        Log.e(
//                            "비디오액티비티", "idx,file_path,recordingVideoFilePath,with,way" +
//                                    idx + " " + file_path + " " + recordingVideoFilePath + " " + with + " " + way
//                        )
//                        startActivity(intent)
//                        finish()

                    }

                }
            }.start()

            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })

            binding.activityRecordBtnStart.visibility= View.GONE
            binding.activityRecordBtnPause.visibility= View.VISIBLE

        }

        initRetrofit()
        binding.btnConvert.setOnClickListener { switchCamera() }

    }

    fun initVideoRecorder() {
        mCamera = Camera.open()
        mCamera!!.setDisplayOrientation(90)
        mSurfaceHolder = binding.surfaceView.getHolder()
        mSurfaceHolder!!.addCallback(this)
        mSurfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun onDestroy() {
        super.onDestroy()
        mCamera!!.lock()
        //job.cancel()
    }

    fun startVideoRecorder() {
        if (isRecording) {
            mRecorder!!.stop()
            mRecorder!!.release()
            mRecorder = null
            mCamera!!.lock()
            isRecording = false

        } else {
            runOnUiThread {
                mRecorder = MediaRecorder()
                mCamera!!.unlock()
                mRecorder!!.setCamera(mCamera)
                mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                mRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
                mRecorder!!.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
                mRecorder!!.setOrientationHint(90)
//                mPath =
//                    Environment.getExternalStorageDirectory().absolutePath + "/record4.mp4"
//                Log.e("메인", "file path is $mPath")
//                file_path=mPath
                mRecorder!!.setOutputFile(recordingVideoFilePath)
                mRecorder!!.setPreviewDisplay(mSurfaceHolder!!.surface)
                try {
                    mRecorder!!.prepare()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mRecorder!!.start()
                isRecording = true

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

//       asyncDialog  = ProgressDialog(this@Video2Activity)
//       /* UI Thread: 프로그래스 바 등 준비 */
//       asyncDialog!!.setProgressStyle(ProgressDialog.BUTTON_POSITIVE)
//       asyncDialog!!.setMessage("믹싱중...")
//       asyncDialog!!.show()

       videoFile = File(recordingVideoFilePath)
       var requestBody : RequestBody = RequestBody.create(
           MediaType.parse("multipart/form-data"), videoFile)
       var body : MultipartBody.Part=
           MultipartBody.Part.createFormData("uploaded_file", fileName, requestBody)
       song_path.let {
           retrofitService.requestMixVideo(it,body).enqueue(object : Callback<String> {
               // 통신에 성공한 경우
               override fun onResponse(call: Call<String>, response: Response<String>) {
                   if (response.isSuccessful) {
                       // 응답을 잘 받은 경우
                       // asyncDialog!!.dismiss()
                       Log.e("비디오2", " 통신 성공: ${response.body().toString()}")
                       // 서버에서 처리된 결과받아야함

                       val jsonArray= JSONArray(response.body().toString())
                       for(i in 0..jsonArray.length() -1){
                           val iObject=jsonArray.getJSONObject(i)
                           val title=iObject.getString("title")
                       }


                       // 업로드 성공 다이얼로그
                       val builder = AlertDialog.Builder(this@Video2Activity)
                       builder.setTitle("SingTogether")
                       builder.setMessage("믹싱을 성공했습니다!")
                       builder.setPositiveButton("확인") { dialogInterface, i ->
   //                               val intent= Intent(applicationContext, AfterRecordActivity::class.java)
   //                               intent.putExtra("MR_IDX", idx)
   //                               intent.putExtra("FILE_PATH", file_path)
   //                               intent.putExtra("USER_PATH", recordingVideoFilePath)
   //                               intent.putExtra("WITH", with)
   //                               intent.putExtra("WAY", way)
   //                               Log.e(
   //                                   "비디오액티비티", "idx,file_path,recordingVideoFilePath,with,way" +
   //                                           idx + " " + file_path + " " + recordingVideoFilePath + " " + with + " " + way
   //                               )
   //                               startActivity(intent)
   //                               finish()
                       }
                       builder.show()

                   } else {
                       // 통신은 성공했지만 응답에 문제가 있는 경우
                       Log.e("비디오2", " 응답 문제" + response.code())
                   }
               }

               override fun onFailure(call: Call<String>, t: Throwable) {
                   Log.e("비디오2", " 통신 실패" + t.message)
               }
           })

       }
   }


    // 녹화한 mp4 에서 오디오 m4a 만 추출 
    suspend fun extract(): String {
        val c = arrayOf (
            "-i", recordingVideoFilePath,
            "-map"  , "0:a" ,  "-c"  , "copy" ,
            //"-c:v", "copy", "-c:a", "aac", "-map", "0:v:0", "-map", "1:a:0", "-shortest","-y",
            extractFilePath
        )
        //ExtractAudio(c)
        FFmpeg.executeAsync(c) { executionId, returnCode ->
            Log.e("hello추출", "return  $returnCode")
            Log.e("hello추출", "executionID  $executionId")
            Log.e("hello추출", "FFMPEG  " + FFmpegExecution(executionId, c))
        }

        return extractFilePath
    }

    private fun ExtractAudio(co: Array<String>) {
        FFmpeg.executeAsync(co) { executionId, returnCode ->
            Log.e("hello추출", "return  $returnCode")
            Log.e("hello추출", "executionID  $executionId")
            Log.e("hello추출", "FFMPEG  " + FFmpegExecution(executionId, co))
        }
        //Merge()
        //videoMerge()
    }

    // 추출한.m4a + mr.m4a = 병합한.m4a 파일
   suspend fun Merge(): String {
        val c = arrayOf (
            "-i", extractFilePath,
            "-i", song_path,
            "-filter_complex",
            "[0][1]amix=inputs=2,pan=stereo|FL<c0+c1|FR<c2+c3[a]",
            "-map",
            "[a]",
            "-y",
            //"-c:v", "copy", "-c:a", "aac", "-map", "0:v:0", "-map", "1:a:0", "-shortest","-y",
            outputVideoFilePath
        )
        //MergeAudio(c)
        FFmpeg.executeAsync(c) { executionId, returnCode ->
            Log.e("hello병합", "return  $returnCode")
            Log.e("hello병합", "executionID  $executionId")
            Log.e("hello병합", "FFMPEG  " + FFmpegExecution(executionId, c))
        }

        return outputVideoFilePath
    }

    private fun MergeAudio(co: Array<String>) {
        FFmpeg.executeAsync(co) { executionId, returnCode ->
            Log.e("hello병합", "return  $returnCode")
            Log.e("hello병합", "executionID  $executionId")
            Log.e("hello병합", "FFMPEG  " + FFmpegExecution(executionId, co))
        }
        //file_path= outputVideoFilePath
        //videoMerge()

    }

    // 병합한.m4a + 녹화한.mp4 = 병합한.mp4
    suspend fun videoMerge(): String {
        val c = arrayOf (
            "-i", recordingVideoFilePath,
            "-i", outputVideoFilePath,
            "-c", "copy", "-shortest", "aac", "-map", "0:v:0", "-map", "1:a:0","-y",
            lastVideoFilePath
        )
        Log.e("새로운비디오", "return" +lastVideoFilePath)
        //MergeVideo(c)
        FFmpeg.executeAsync(c) { executionId, returnCode ->
            Log.e("hello비디오", "return  $returnCode")
            Log.e("hello비디오", "executionID  $executionId")
            Log.e("hello비디오", "FFMPEG  " + FFmpegExecution(executionId, c))
        }
        file_path= lastVideoFilePath
//        val intent= Intent(applicationContext, AfterRecordActivity::class.java)
//        intent.putExtra("MR_IDX", idx)
//        intent.putExtra("FILE_PATH", file_path)
//        intent.putExtra("USER_PATH", recordingVideoFilePath)
//        intent.putExtra("WITH", with)
//        intent.putExtra("WAY", way)
//        Log.e(
//            "비디오액티비티", "idx,file_path,recordingVideoFilePath,with,way" +
//                    idx + " " + file_path + " " + recordingVideoFilePath + " " + with + " " + way
//        )
//        startActivity(intent)
//        finish()

        return lastVideoFilePath
    }

    private fun MergeVideo(co: Array<String>) {
        FFmpeg.executeAsync(co) { executionId, returnCode ->
            Log.e("hello비디오", "return  $returnCode")
            Log.e("hello비디오", "executionID  $executionId")
            Log.e("hello비디오", "FFMPEG  " + FFmpegExecution(executionId, co))
        }
        file_path= lastVideoFilePath
        val intent= Intent(applicationContext, AfterRecordActivity::class.java)
        intent.putExtra("MR_IDX", idx)
        intent.putExtra("FILE_PATH", file_path)
        intent.putExtra("USER_PATH", recordingVideoFilePath)
        intent.putExtra("WITH", with)
        intent.putExtra("WAY", way)
        Log.e(
            "비디오액티비티", "idx,file_path,recordingVideoFilePath,with,way" +
                    idx + " " + file_path + " " + recordingVideoFilePath + " " + with + " " + way
        )
        startActivity(intent)
        finish()

    }

    // 병합한.m4a + 녹화한.mp4 = 병합한.mp4
    fun videoMerge1() {
        val c = arrayOf (
            "-i", recordingVideoFilePath,
            "-i", outputVideoFilePath,
            "-c:v", "copy", "-c:a", "aac", "-map", "0:v:0", "-map", "1:a:0", "-shortest","-y",
            lastVideoFilePath
        )
        Log.e("새로운비디오", "return" +lastVideoFilePath)
        MergeVideo1(c)
    }

    private fun MergeVideo1(co: Array<String>) {
        FFmpeg.executeAsync(co) { executionId, returnCode ->
            Log.e("hello비디오", "return  $returnCode")
            Log.e("hello비디오", "executionID  $executionId")
            Log.e("hello비디오", "FFMPEG  " + FFmpegExecution(executionId, co))
        }
        file_path= lastVideoFilePath
        val intent= Intent(applicationContext, AfterRecordActivity::class.java)
        intent.putExtra("MR_IDX", idx)
        intent.putExtra("FILE_PATH", file_path)
        intent.putExtra("USER_PATH", recordingVideoFilePath)
        intent.putExtra("WITH", with)
        intent.putExtra("WAY", way)
        Log.e(
            "비디오액티비티", "idx,file_path,recordingVideoFilePath,with,way" +
                    idx + " " + file_path + " " + recordingVideoFilePath + " " + with + " " + way
        )
        startActivity(intent)
        finish()
    }

    // 카메라 전환
    private fun switchCamera() {
        when(mCameraId){
            Video2Activity.CAMERA_BACK -> {
                mCameraId = Video2Activity.CAMERA_FRONT
                mCameraDevice.close()

                //openCamera()
            }
            else -> {
                mCameraId = Video2Activity.CAMERA_BACK
                mCameraDevice.close()
                //openCamera()
            }
        }
    }



    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {


    }



}