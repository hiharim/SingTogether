package com.harimi.singtogether.sing

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.hardware.Camera
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
    private lateinit var mr_path : String // 노래 mr
    private lateinit var duet_path : String // 사용자 비디오
    private lateinit var extract_path : String // 병합하고자하는 영상의 추출된 오디오
    lateinit var mediaPlayer: MediaPlayer
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMergeBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        Log.e("MergeActivity", "가사 : " + lyrics)
        val array = lyrics?.split(" ★".toRegex())?.toTypedArray()
        if (array != null) {
            for (i in array.indices) {
                println(array[i])
                val seconds=array[i]
                //7
                val line= array[i].substring(6)
                val lyricsData= LyricsData(seconds, line)
                lyricsList.add(lyricsData)

            }
        }
        //리사이클러뷰 설정
        binding.activityMergeRv.layoutManager= LinearLayoutManager(applicationContext)
        binding.activityMergeRv.setHasFixedSize(true)
        binding.activityMergeRv.setBackgroundColor(Color.parseColor("#81000000"))

        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(duet_path)
        mediaPlayer.prepare()

        initVideoRecorder()
        initRetrofit()
        // 마이크 버튼 클릭
        binding.activityRecordBtnStart.setOnClickListener {
            // 노래 재생
            mediaPlayer.start()

            startVideoRecorder()

            /* 실시간으로 변경되는 진행시간과 시크바를 구현하기 위한 스레드 사용*/
            object : Thread() {
                var timeFormat2 = android.icu.text.SimpleDateFormat("m.ss")  //"분:초"를 나타낼 수 있도록 포멧팅
                var timeFormat = android.icu.text.SimpleDateFormat("mm:ss")  //"분:초"를 나타낼 수 있도록 포멧팅
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

                            binding.activityRecordTvPlayTime.text=timeFormat2.format(mediaPlayer.currentPosition)
                            RecordActivity.time_info.pTime= binding.activityRecordTvPlayTime.text.toString()
                            lyricsAdapter= LyricsAdapter(lyricsList)
                            binding.activityMergeRv.adapter=lyricsAdapter
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
                        try{
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
    }

    // 서버에서 mix
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

    // 비디오+비디오
//    fun Merge() {
//        val c = arrayOf(
//            "-i",
//            mergeVideoFilePath,
//            "-i",
//            duet_path,
//            "-filter_complex",
//            "[0:v]pad=iw*2:ih[int];[int][1:v]overlay=W/2:0[vid]",
//            "-map",
//            "[vid]",
//            "-c:v",
//            "libx264",
//            "-shortest",
//            "-y",
//            "-crf",
//            "23",
//            "-preset",
//            "veryfast",
//            finishVideoFilePath
//        )
//        Log.e("Merge()", "mergeVideoFilePath" + mergeVideoFilePath)
//        Log.e("Merge()", "duet_path" + duet_path)
//        MergeAudio(c)
//    }
//
//    private fun MergeAudio(co: Array<String>) {
//        FFmpeg.executeAsync(co) { executionId, returnCode ->
//            Log.e("hello병합", "return  $returnCode")
//            Log.e("hello병합", "executionID  $executionId")
//            Log.e("hello병합", "FFMPEG  " + FFmpegExecution(executionId, co))
//        }
//        file_path=finishVideoFilePath
//        //file_path=mergeVideoFilePath
//    }

    // 비디오.mp4 + 비디오.mp4 = output 확장자 .mp4
//        public void Merge(View view) {
//        String[] c = {"-i", Environment.getExternalStorageDirectory().getPath()
//                + "/Download/hi_1.mp4"
//                , "-i", Environment.getExternalStorageDirectory().getPath()
//                + "/Download/Mp4.mp4"
//                , "-filter_complex", "[0:v]pad=iw*2:ih[int];[int][1:v]overlay=W/2:0[vid]", "-map","[vid]", "-c:v","libx264", "-shortest","-y",
//                "-crf","23", "-preset", "veryfast",
//                Environment.getExternalStorageDirectory().getPath()
//                        + "/Download/video merge.mp4"};
//            Log.e("새로운비디오", "return  " + Environment.getExternalStorageDirectory().getPath()+ "/Download/video merge.mp4");
//        MergeVideo(c);
//    }

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

    // 녹화
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
                mRecorder!!.setOutputFile(mergeVideoFilePath)
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

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {


    }
}


