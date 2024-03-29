package com.harimi.singtogether.sing

import android.app.ProgressDialog
import android.content.Intent
import android.icu.text.DecimalFormat
import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
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
import com.harimi.singtogether.databinding.ActivityMergeAudioBinding
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

/***
 * 듀엣 녹음하는 화면
 * Audio merge
 */

class MergeAudioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMergeAudioBinding
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
    private lateinit var collaborationNickname : String // 병합하고자하는 유저의 닉네임
    private lateinit var collaborationEmail : String // 병합하고자하는 유저의 이메일
    private val lyricsList : ArrayList<LyricsData> = ArrayList()
    private lateinit var lyricsAdapter: LyricsAdapter
    private var audioFile : File?=null
    lateinit var fileName : String // 서버로 보낼 오디오 파일 이름
    private var mediaPlayer: MediaPlayer? = null
    private var recorder : MediaRecorder?=null // 사용하지 않을 때는 메모리 해제 및 null 처리
    private val recordingFilePath :String by lazy {
        "${externalCacheDir?.absolutePath}/merge_recording.m4a"
    }
    var asyncDialog : ProgressDialog?=null
    private val timeList: java.util.ArrayList<String> = java.util.ArrayList()
    private val nextList: java.util.ArrayList<String> = java.util.ArrayList()
    private var pausePosition : Int ?=null
    private var finishPosition : Int ?=null
    private var isFinished=false
    private var isPaused=false
    private lateinit var beforeTotalTime : String
    private var realBeforeTotalTime : String?=null
    private var isRecording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMergeAudioBinding.inflate(layoutInflater)
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

        binding.activityRecordRv.layoutManager= LinearLayoutManager(applicationContext)
        binding.activityRecordRv.setHasFixedSize(true)

//        mediaPlayer = MediaPlayer()
//        mediaPlayer.setDataSource(duet_path)
//        mediaPlayer.prepare()

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
                recorder!!.pause()
                isPaused=true
            }
        }

        // 닫기 버튼 클릭
        binding.fragmentMergeAudioBtnClose.setOnClickListener {
            if (!isPaused) {
                binding.activityRecordBtnStart.visibility = View.VISIBLE
                binding.activityRecordBtnPause.visibility = View.GONE
                mediaPlayer?.pause()
                pausePosition=mediaPlayer?.currentPosition
                recorder!!.pause()
                isPaused=true
            }
            val builder = AlertDialog.Builder(this)
            builder.setTitle("녹음을 종료하시겠습니까? ")
            builder.setMessage("지금 녹음을 종료하시면 저장되지 않습니다.")
            builder.setPositiveButton("네") { dialog, which ->
                recorder?.release()
                recorder=null
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
            binding.activityRecordBtnPause.visibility=View.VISIBLE
           // mediaPlayer.start()
            if(!isPaused){
                mediaPlayer = MediaPlayer()
                mediaPlayer?.setDataSource(duet_path)
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                Record()
                isFinished=false
            }else{
                // resume
                binding.activityRecordBtnStart.visibility = View.GONE
                binding.activityRecordBtnPause.visibility = View.VISIBLE
                mediaPlayer?.seekTo(pausePosition!!)
                mediaPlayer?.start()
                recorder!!.resume()
                isPaused=false

            }

            /* 실시간으로 변경되는 진행시간과 시크바를 구현하기 위한 스레드 사용*/
            object : Thread() {
                var timeFormat2 = SimpleDateFormat("m.ss")  //"분:초"를 나타낼 수 있도록 포멧팅
                var timeFormat = SimpleDateFormat("mm:ss")  //"분:초"를 나타낼 수 있도록 포멧팅
                override fun run() {
                    super.run()
                    if (mediaPlayer == null)
                        return
                    binding.seekBar.max = mediaPlayer!!.duration  // mPlayer.duration : 음악 총 시간

                    while (mediaPlayer!!.isPlaying) {
                        runOnUiThread { //화면의 위젯을 변경할 때 사용 (이 메소드 없이 아래 코드를 추가하면 실행x)
                            binding.seekBar.progress = mediaPlayer!!.currentPosition
                            binding.activityRecordTvIngTime.text = timeFormat.format(mediaPlayer!!.currentPosition)
                            binding.activityRecordTvTotalTime.text=timeFormat.format(mediaPlayer!!.duration)
                            binding.activityRecordTvPlayTime.text=timeFormat2.format(mediaPlayer!!.currentPosition)
                            RecordActivity.time_info.pTime= binding.activityRecordTvPlayTime.text.toString()

                            val minusSecond=mediaPlayer!!.duration-1000
                            realBeforeTotalTime=timeFormat.format(minusSecond).toString()

                            lyricsAdapter= LyricsAdapter(lyricsList)
                            binding.activityRecordRv.adapter=lyricsAdapter
                            for(i in timeList) {
                                var minus_one=i.toFloat()-0.01.toFloat()
                                val t_down = DecimalFormat("0.00")
                                var second = t_down.format(minus_one)
                                var mTime=t_down.format(RecordActivity.time_info.pTime.toFloat())

                                for(j in nextList) {
                                    var nTime = j.toFloat()
                                    var nMunusTime=j.toFloat()-0.01.toFloat()
                                    var nextTime = t_down.format(nTime)
                                    var nextMinusTime=t_down.format(nMunusTime)

                                    if (mTime.toString() == nextTime.toString() && second==nextMinusTime) {
                                        lyricsList.removeAt(0)
                                        lyricsAdapter.notifyItemRemoved(0)
                                    }

                                }
                            }

                        }
                        SystemClock.sleep(1000)
                    }

                    if(!mediaPlayer!!.isPlaying){
                        isFinished=true
                    }

                    if(isFinished) {
                        mediaPlayer?.stop() // 음악 정지
                        mediaPlayer?.release()

                        recorder!!.stop()
                        recorder!!.release()
                        recorder = null
                        try{
                            Thread(Runnable {
                                // ==== [UI 동작 실시] ====
                                runOnUiThread {
                                    asyncDialog = ProgressDialog(this@MergeAudioActivity)
                                    asyncDialog!!.setProgressStyle(ProgressDialog.BUTTON_POSITIVE)
                                    asyncDialog!!.setMessage("믹싱중...")
                                    asyncDialog!!.show()
                                }
                            }).start()
                        }
                        catch (e: Exception){
                            e.printStackTrace()
                        }
                        mergeAudio()
                    }

                    // 음악이 종료되면 녹음 중지하고 AfterSingActivity 로 이동
//                    if(!mediaPlayer.isPlaying) {
//                        mediaPlayer.stop() // 음악 정지
//                        mediaPlayer.release()
//
//                        recorder!!.stop()
//                        recorder!!.release()
//                        recorder = null
//                        try{
//                            Thread(Runnable {
//                                // ==== [UI 동작 실시] ====
//                                runOnUiThread {
//                                    asyncDialog = ProgressDialog(this@MergeAudioActivity)
//                                    asyncDialog!!.setProgressStyle(ProgressDialog.BUTTON_POSITIVE)
//                                    asyncDialog!!.setMessage("믹싱중...")
//                                    asyncDialog!!.show()
//                                }
//                            }).start()
//                        }
//                        catch (e: Exception){
//                            e.printStackTrace()
//                        }
//                        mergeAudio()
//
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
            // 녹음 시작
            //Record()

        }

    }

    fun Record() {
        if(isFinished){
            recorder!!.stop()
            recorder!!.release()
            recorder = null
            isRecording = false
        }else{
            runOnUiThread {
                recorder= MediaRecorder()
                recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder!!.setOutputFile(recordingFilePath) // 외부 캐시 디렉토리에 임시적으로 저장 ,위에 선언해둔 외부 캐시 FilePath 를 이용
                try {
                    recorder!!.prepare()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                recorder!!.start()
                isRecording = true
                isFinished=false
            }
        }
    }

    // 사용자 음성 녹음
//    fun Record() {
//        recorder = MediaRecorder().apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//            setOutputFile(recordingFilePath) // 외부 캐시 디렉토리에 임시적으로 저장 ,위에 선언해둔 외부 캐시 FilePath 를 이용
//            prepare()
//        }
//        recorder?.start()
//    }

    private fun mergeAudio() {
        val timeStamp : String = java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        fileName = "$timeStamp.m4a"
        val email= LoginActivity.user_info.loginUserEmail
        audioFile = File(recordingFilePath)
        var requestBody : RequestBody = RequestBody.create(
            MediaType.parse("multipart/form-data"), audioFile
        )
        var body : MultipartBody.Part=
            MultipartBody.Part.createFormData("uploaded_file", fileName, requestBody)
        recordingFilePath.let {
            retrofitService.requestMergeAudio(mr_path, email, collaborationEmail,extract_path,body).enqueue(object : Callback<String> {
                // 통신에 성공한 경우
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    var output_path: String? = null
                    var circle_profile: String? = null
                    if (response.isSuccessful) {
                        // 응답을 잘 받은 경우
                        asyncDialog!!.dismiss()
                        Log.e("MergeAudioActivity", "body : " + response.body().toString())
                        val jsonObject = JSONObject(response.body().toString())
                        output_path = "http://3.35.236.251/" + jsonObject.getString("output_path")
                        circle_profile = "http://3.35.236.251/" + jsonObject.getString("merge_thumbnail_path")
                        Log.e("MergeAudioActivity", " output_path" +output_path)
                        Log.e("MergeAudioActivity", " circle_profile" + circle_profile)
                        // 믹싱 성공 다이얼로그
                        val builder = AlertDialog.Builder(this@MergeAudioActivity)
                        builder.setTitle("SingTogether")
                        builder.setMessage("믹싱을 성공했습니다!")
                        builder.setPositiveButton("확인") { dialogInterface, i ->
                            val intent = Intent(applicationContext, AfterSingActivity::class.java)
                            intent.putExtra("DUET_IDX", duet_idx)
                            intent.putExtra("MR_IDX", mr_idx)
                            intent.putExtra("FILE_PATH", output_path)
                            intent.putExtra("USER_PATH", extract_path)
                            intent.putExtra("WITH", with)
                            intent.putExtra("WAY", way)
                            intent.putExtra("CIRCLE_PROFILE", circle_profile)
                            intent.putExtra("MERGE", "Y")
                            intent.putExtra("COLLABORATION_NICKNAME", collaborationNickname)
                            intent.putExtra("COLLABO_EMAIL", collaborationEmail)
                            startActivity(intent)
                            finish()

                        }
                        builder.show()

                    } else {
                        // 통신은 성공했지만 응답에 문제가 있는 경우
                        Log.e("MergeAudioActivity", " 응답 문제" + response.code())
                        when (response.code()) {
                            404 -> fail()
                        }
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("MergeAudioActivity", " 통신 실패" + t.message)
                }
            })

        }
    }

    // 앱이 백그라운드로 넘어가도 음악이 계속 실행되는거 막기 위해서 오버라이드
    override fun onStop() {
        super.onStop()
        mediaPlayer?.release()
        mediaPlayer=null
    }

    override fun onDestroy(){
        super.onDestroy()
        mediaPlayer?.release()
    }

    fun fail(){
        Log.e("MergeAudioActivity", " 통신 실패" + 404)
    }

//    class time_info{
//        companion object {
//            var pTime = ""
//        }
//    }

    // 레트로핏 초기화
    private fun initRetrofit(){
        retrofit= RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

}