package com.harimi.singtogether.sing

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.icu.text.DecimalFormat
import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
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
import com.harimi.singtogether.databinding.ActivityRecordBinding
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * 녹음하는 액티비티
 * */
class RecordActivity: AppCompatActivity()  {

    private lateinit var binding: ActivityRecordBinding
    private lateinit var retrofit: Retrofit
    private lateinit var retrofitService: RetrofitService
    private var idx : Int? = null
    private var title : String? = null
    private var singer : String? = null
    private var with : String? = null
    private var way : String? = null
    private var lyrics : String? = null // 가사
    private lateinit var song_path : String
    lateinit var mediaPlayer: MediaPlayer
    private var recorder : MediaRecorder?=null // 사용하지 않을 때는 메모리 해제 및 null 처리
    private val recordingFilePath :String by lazy {
        "${externalCacheDir?.absolutePath}/recording.m4a"
    }

    var asyncDialog : ProgressDialog?=null
    private var audioFile : File?=null
    private var file_path:String?=null
    lateinit var fileName : String // 서버로 보낼 오디오 파일 이름
    //private var nickname : String? = null
    private val lyricsList : ArrayList<LyricsData> = ArrayList()
    private lateinit var lyricsAdapter: LyricsAdapter

    private val timeList:ArrayList<String> = ArrayList()
    private val nextList:ArrayList<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRetrofit()

        idx=intent.getIntExtra("RECORD_IDX", 0)
        title=intent.getStringExtra("RECORD_TITLE")
        singer=intent.getStringExtra("RECORD_SINGER")
        lyrics=intent.getStringExtra("RECORD_LYRICS")
        with=intent.getStringExtra("WITH")
        way=intent.getStringExtra("WAY")
        song_path= intent.getStringExtra("RECORD_SONG_PATH").toString()
        Log.e("RecordActivity", " idx" + idx)
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
//                val line= array[i].substring(6)
//                val lyricsData=LyricsData(seconds, line)
//                lyricsList.add(lyricsData)
//
//                val times=array[i].substring(1,5)
//                Log.e("레코드액티비티","times"+times)
//                timeList.add(times)
//
//                val next=array[i].substring(1,5)
//                nextList.add(next)

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
        Log.e("레코드액티비티","next"+nextList)

        binding.activityRecordRv.layoutManager= LinearLayoutManager(applicationContext)
        binding.activityRecordRv.setHasFixedSize(true)
        //binding.activityRecordRv.setBackgroundColor(Color.parseColor("#81000000"))

        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(song_path)
        mediaPlayer.prepare()

        val dialog = EarPhoneDialog(this)
        dialog.myDig()
        // 마이크 버튼 클릭
        binding.activityRecordBtnStart.setOnClickListener {
            // 노래 재생
            mediaPlayer.start()

            /* 실시간으로 변경되는 진행시간과 시크바를 구현하기 위한 스레드 사용*/
            object : Thread() {
                var timeFormat2 = SimpleDateFormat("m.ss")  //"분:초"를 나타낼 수 있도록 포멧팅
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

                            binding.activityRecordTvPlayTime.text=timeFormat2.format(mediaPlayer.currentPosition)
                            time_info.pTime= binding.activityRecordTvPlayTime.text.toString()
                            //time=binding.activityRecordTvIngTime.text.toString()
                            lyricsAdapter= LyricsAdapter(lyricsList)
                            binding.activityRecordRv.adapter=lyricsAdapter

                            for(i in timeList) {
                                var minus_one=i.toFloat()-0.01.toFloat()
                                val t_down = DecimalFormat("0.00")
                                var second = t_down.format(minus_one)
                                var mTime=t_down.format(time_info.pTime.toFloat())

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

                    // 음악이 종료되면 녹음 중지하고 AfterSingActivity 로 이동
                    if(!mediaPlayer.isPlaying) {
                        mediaPlayer.stop() // 음악 정지
                        mediaPlayer.release()
                       // recordStop() // 녹음 중지

                        recorder!!.stop()
                        recorder!!.release()
                        recorder = null
                        try{
                            Thread(Runnable {
                                // ==== [UI 동작 실시] ====
                                runOnUiThread {
                                    asyncDialog = ProgressDialog(this@RecordActivity)
                                    asyncDialog!!.setProgressStyle(ProgressDialog.BUTTON_POSITIVE)
                                    asyncDialog!!.setMessage("믹싱중...")
                                    asyncDialog!!.show()
                                }
                            }).start()
                        }
                        catch (e: Exception){
                            e.printStackTrace()
                        }
                        mixAudio()

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
            // 녹음 시작
            Record()

            binding.activityRecordBtnStart.visibility= View.GONE
            binding.activityRecordBtnPause.visibility=View.VISIBLE

        }
        // 중지버튼
        binding.activityRecordBtnPause.setOnClickListener {
            mediaPlayer.pause()

        }


    }

    // 사용자 음성 녹음
    fun Record() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(recordingFilePath) // 외부 캐시 디렉토리에 임시적으로 저장 ,위에 선언해둔 외부 캐시 FilePath 를 이용
            prepare()
        }
        recorder?.start()
    }

    fun recordStop() {
        recorder?.run {
            stop()
            release()
        }
        recorder=null
        //Toast.makeText(applicationContext, "녹음중지", Toast.LENGTH_SHORT).show()
        //Merge()
    }


    private fun mixAudio() {
        val timeStamp : String = java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        fileName = "$timeStamp.m4a"
        //val nickname= LoginActivity.user_info.loginUserNickname
        val email= LoginActivity.user_info.loginUserEmail
        audioFile = File(recordingFilePath)
        var requestBody : RequestBody = RequestBody.create(
            MediaType.parse("multipart/form-data"), audioFile
        )
        var body : MultipartBody.Part=
            MultipartBody.Part.createFormData("uploaded_file", fileName, requestBody)
        song_path.let {
            retrofitService.requestMixAudio(it, email, body).enqueue(object : Callback<String> {
                // 통신에 성공한 경우
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    var mix_path: String? = null
                    var extract_path: String? = null
                    var circle_profile: String? = null
                    if (response.isSuccessful) {
                        // 응답을 잘 받은 경우
                        asyncDialog!!.dismiss()
                        val jsonObject = JSONObject(response.body().toString())
                        mix_path = "http://3.35.236.251/" + jsonObject.getString("mix_path")
                        extract_path = "http://3.35.236.251/" + jsonObject.getString("extract_path")
                        circle_profile =
                            "http://3.35.236.251/" + jsonObject.getString("circle_profile");
                        Log.e("레코드액티비티", " mix_path" + mix_path)
                        Log.e("레코드액티비티", " circle_profile" + circle_profile)
                        // 믹싱 성공 다이얼로그
                        val builder = AlertDialog.Builder(this@RecordActivity)
                        builder.setTitle("SingTogether")
                        builder.setMessage("믹싱을 성공했습니다!")
                        builder.setPositiveButton("확인") { dialogInterface, i ->
                            val intent = Intent(applicationContext, AfterSingActivity::class.java)
                            intent.putExtra("MR_IDX", idx)
                            intent.putExtra("FILE_PATH", mix_path)
                            intent.putExtra("USER_PATH", extract_path)
                            intent.putExtra("WITH", with)
                            intent.putExtra("WAY", way)
                            intent.putExtra("CIRCLE_PROFILE", circle_profile)
                            intent.putExtra("MERGE", "N")
                            intent.putExtra("COLLABORATION_NICKNAME", "NULL")
                            intent.putExtra("COLLABO_EMAIL", "NULL")
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

    // 오디오.m4a + 오디오.m4a = output.m4a 오디오
//    fun Merge() {
//        getInfo()
//        val c = arrayOf (
//            "-i", recordingFilePath,
//            "-i", song_path,
//            "-filter_complex",
//            "[0][1]amix=inputs=2,pan=stereo|FL<c0+c1|FR<c2+c3[a]",
//            "-map",
//            "[a]",
//            "${externalCacheDir?.absolutePath}/UserMrAudio.m4a"
//        )
//        Log.e("새로운오디오", "return" + "${externalCacheDir?.absolutePath}/newUserMrAudio.m4a")
//        MergeAudio(c)
//    }
//
//    fun getInfo(){
//        val c = arrayOf (
//            "-i", recordingFilePath,
//            "-filter",
//            "volumedetect",
//            "-f",
//            "null"
//        )
//        Log.e("getInfo", "recordingFilePath" )
//        FFmpeg.executeAsync(c) { executionId, returnCode ->
//            Log.d("getInfo", "return  $returnCode")
//            Log.d("getInfo", "executionID  $executionId")
//            Log.d("getInfo", "FFMPEG  " + FFmpegExecution(executionId, c))
//        }
//    }
//
//    private fun MergeAudio(co: Array<String>) {
//        FFmpeg.executeAsync(co) { executionId, returnCode ->
//            Log.d("hello", "return  $returnCode")
//            Log.d("hello", "executionID  $executionId")
//            Log.d("hello", "FFMPEG  " + FFmpegExecution(executionId, co))
//        }
//        file_path= "${externalCacheDir?.absolutePath}/UserMrAudio.m4a"
//    }

    override fun onDestroy(){
        super.onDestroy()
        mediaPlayer?.release()
    }

    // 레트로핏 초기화
    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

}
