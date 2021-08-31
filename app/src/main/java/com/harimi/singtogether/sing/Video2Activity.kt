package com.harimi.singtogether.sing

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
import androidx.appcompat.app.AppCompatActivity
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivityVideo2Binding
import java.text.SimpleDateFormat

class Video2Activity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var binding: ActivityVideo2Binding
    private var idx : Int? = null
    private var title : String? = null
    private var singer : String? = null
    private var with : String? = null
    private var way : String? = null
    private var lyrics : String? = null // 가사
    private lateinit var song_path : String
    lateinit var mediaPlayer: MediaPlayer
    private var  mRecorder : MediaRecorder?=null // 사용하지 않을 때는 메모리 해제 및 null 처리
    private var isRecording = false
    private var mPath: String? = null
    var mSurfaceHolder: SurfaceHolder? = null
    var mCamera: Camera? = null
    private val recordingVideoFilePath :String by lazy {
        "${externalCacheDir?.absolutePath}/recordVideo2.mp4"
    }
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

        binding.btnConvert.setOnClickListener { switchCamera() }

    }
    fun initVideoRecorder() {
        mCamera = Camera.open()
        mCamera!!.setDisplayOrientation(90)
        mSurfaceHolder = binding.surfaceView.getHolder()
        mSurfaceHolder!!.addCallback(this)
        mSurfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

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
                mPath =
                    Environment.getExternalStorageDirectory().absolutePath + "/record4.mp4"
                Log.e("메인", "file path is $mPath")
                file_path=mPath
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