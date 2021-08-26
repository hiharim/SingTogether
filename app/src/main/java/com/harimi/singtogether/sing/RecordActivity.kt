package com.harimi.singtogether.sing

import android.content.Intent
import android.graphics.Camera
import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFmpegExecution
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivityRecordBinding

class RecordActivity: AppCompatActivity()  {

    private lateinit var binding: ActivityRecordBinding
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

    private var file_path:String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idx=intent.getIntExtra("RECORD_IDX",0)
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
       // var splitArray= lyrics?.split(" ★")
        var result=lyrics?.replace(" ★","\n")
        binding.activityRecordTvLyrics.text= result.toString()

        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(song_path)
        mediaPlayer.prepare()
        // 마이크 버튼 클릭
        binding.activityRecordBtnStart.setOnClickListener {
            // 노래 재생
            mediaPlayer.start()

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
                        recordStop() // 녹음 중지

                        val intent= Intent(applicationContext,AfterSingActivity::class.java)
                        intent.putExtra("MR_IDX",idx)
                        intent.putExtra("FILE_PATH",file_path)
                        intent.putExtra("USER_PATH",recordingFilePath)
                        intent.putExtra("WITH",with)
                        intent.putExtra("WAY",way)
                        startActivity(intent)
                        finish()
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
        Toast.makeText(applicationContext, "녹음시작", Toast.LENGTH_SHORT).show()
    }

    fun recordStop() {
        recorder?.run {
            stop()
            release()
        }
        recorder=null
        //Toast.makeText(applicationContext, "녹음중지", Toast.LENGTH_SHORT).show()
        Merge()
    }

    // 오디오.m4a + 오디오.m4a = output.m4a 오디오
    fun Merge() {
        getInfo()
        val c = arrayOf (
            "-i", recordingFilePath,
            "-i", song_path,
            "-filter_complex",
            "[0][1]amix=inputs=2,pan=stereo|FL<c0+c1|FR<c2+c3[a]",
            "-map",
            "[a]",
            "${externalCacheDir?.absolutePath}/UserMrAudio.m4a"
        )
        Log.e("새로운오디오", "return" + "${externalCacheDir?.absolutePath}/newUserMrAudio.m4a")
        MergeAudio(c)
    }

    fun getInfo(){
        val c = arrayOf (
            "-i", recordingFilePath,
            "-filter",
            "volumedetect",
            "-f",
            "null"
        )
        Log.e("getInfo", "recordingFilePath" )
        FFmpeg.executeAsync(c) { executionId, returnCode ->
            Log.d("getInfo", "return  $returnCode")
            Log.d("getInfo", "executionID  $executionId")
            Log.d("getInfo", "FFMPEG  " + FFmpegExecution(executionId, c))
        }
    }

    private fun MergeAudio(co: Array<String>) {
        FFmpeg.executeAsync(co) { executionId, returnCode ->
            Log.d("hello", "return  $returnCode")
            Log.d("hello", "executionID  $executionId")
            Log.d("hello", "FFMPEG  " + FFmpegExecution(executionId, co))
        }
        file_path= "${externalCacheDir?.absolutePath}/UserMrAudio.m4a"
    }

    override fun onDestroy(){
        super.onDestroy()
        mediaPlayer?.release()
    }


}
