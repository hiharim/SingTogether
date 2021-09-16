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
import android.os.SystemClock
import android.util.Log
import android.util.SparseIntArray
import android.view.SurfaceHolder
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivityVideo2Binding
import kotlinx.coroutines.*
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
           MediaType.parse("multipart/form-data"), videoFile
       )
       var body : MultipartBody.Part=
           MultipartBody.Part.createFormData("uploaded_file", fileName, requestBody)
       song_path.let {
           retrofitService.requestMixVideo(it, body).enqueue(object : Callback<String> {
               // 통신에 성공한 경우
               override fun onResponse(call: Call<String>, response: Response<String>) {
                   var extract_path : String? = null
                   var finish_path : String ?=null
                   if (response.isSuccessful) {
                       // 응답을 잘 받은 경우
                       // asyncDialog!!.dismiss()

                       val jsonObject = JSONObject(response.body().toString())
                       extract_path= "http://3.35.236.251/"+jsonObject.getString("extract_path")
                       finish_path= "http://3.35.236.251/"+jsonObject.getString("finish_path");

                       // 믹싱 성공 다이얼로그
                       val builder = AlertDialog.Builder(this@Video2Activity)
                       builder.setTitle("SingTogether")
                       builder.setMessage("믹싱을 성공했습니다!")
                       builder.setPositiveButton("확인") { dialogInterface, i ->
                           val intent = Intent(applicationContext, AfterRecordActivity::class.java)
                           intent.putExtra("MR_IDX", idx)
                           intent.putExtra("FILE_PATH", finish_path) // 최종완성된 비디오 파일 path
                           intent.putExtra("USER_PATH", extract_path) // 추출한 오디오 파일 path
                           intent.putExtra("WITH", with)
                           intent.putExtra("WAY", way)
                           intent.putExtra("MERGE", "N")
                           intent.putExtra("COLLABORATION_NICKNAME", "NULL")
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
        Log.e("비디오2", " 통신 실패" +404)
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