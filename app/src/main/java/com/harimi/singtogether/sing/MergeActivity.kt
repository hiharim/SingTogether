package com.harimi.singtogether.sing

import android.content.Intent
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.SeekBar
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFmpegExecution
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivityMergeBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat

class MergeActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var binding: ActivityMergeBinding
    private var idx : Int? = null
    private var title : String? = null
    private var singer : String? = null
    private var with : String? = null
    private var way : String? = null
    private var lyrics : String? = null // 가사
    private lateinit var mr_path : String // 노래 mr
    private lateinit var duet_path : String // 사용자 오디오
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
    private val finishVideoFilePath :String by lazy {
        "${externalCacheDir?.absolutePath}/finishVideo8.mp4"
    }
    private var file_path:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMergeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idx=intent.getIntExtra("RECORD_IDX", 0)
        title=intent.getStringExtra("RECORD_TITLE")
        singer=intent.getStringExtra("RECORD_SINGER")
        lyrics=intent.getStringExtra("RECORD_LYRICS")
        with=intent.getStringExtra("WITH")
        way=intent.getStringExtra("WAY")
        mr_path= intent.getStringExtra("RECORD_MR_PATH").toString()
        duet_path= intent.getStringExtra("RECORD_SONG_PATH").toString()

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
        mediaPlayer.setDataSource(mr_path)
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

                        Merge()
                        val intent= Intent(applicationContext, AfterRecordActivity::class.java)
                        intent.putExtra("MR_IDX", idx)
                        intent.putExtra("FILE_PATH", file_path)
                        intent.putExtra("USER_PATH", mergeVideoFilePath)
                        intent.putExtra("WITH", with)
                        intent.putExtra("WAY", way)
                        Log.e(
                            "머지액티비티", "idx,file_path,mergeVideoFilePath,with,way" +
                                    idx + " " + file_path + " " + mergeVideoFilePath + " " + with + " " + way
                        )
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

            binding.activityRecordBtnStart.visibility= View.GONE
            binding.activityRecordBtnPause.visibility= View.VISIBLE

        }


    }


    // 비디오+비디오
    fun Merge() {
        val c = arrayOf(
            "-i",
            mergeVideoFilePath,
            "-i",
            duet_path,
            "-filter_complex",
            "[0:v]pad=iw*2:ih[int];[int][1:v]overlay=W/2:0[vid]",
            "-map",
            "[vid]",
            "-c:v",
            "libx264",
            "-shortest",
            "-y",
            "-crf",
            "23",
            "-preset",
            "veryfast",
            finishVideoFilePath
        )
        Log.e("Merge()", "mergeVideoFilePath" + mergeVideoFilePath)
        Log.e("Merge()", "duet_path" + duet_path)
        MergeAudio(c)
    }

    private fun MergeAudio(co: Array<String>) {
        FFmpeg.executeAsync(co) { executionId, returnCode ->
            Log.e("hello병합", "return  $returnCode")
            Log.e("hello병합", "executionID  $executionId")
            Log.e("hello병합", "FFMPEG  " + FFmpegExecution(executionId, co))
        }
        file_path=finishVideoFilePath
        //file_path=mergeVideoFilePath

    }

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
                //                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                //                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                //                    mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
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

