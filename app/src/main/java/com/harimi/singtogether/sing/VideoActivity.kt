package com.harimi.singtogether.sing

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.drm.DrmStore.Playback.STOP
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.*
import android.media.*
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.SurfaceHolder
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivityRecordBinding
import com.harimi.singtogether.databinding.ActivityVideoBinding
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat

class VideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoBinding
    private var idx : Int? = null
    private var title : String? = null
    private var singer : String? = null
    private var with : String? = null
    private var way : String? = null
    private var lyrics : String? = null // 가사
    private lateinit var song_path : String
    lateinit var mediaPlayer: MediaPlayer
    private var recorder : MediaRecorder?=null // 사용하지 않을 때는 메모리 해제 및 null 처리
    private val recordingVideoFilePath :String by lazy {
        "${externalCacheDir?.absolutePath}/recordingVideo.mp4"
    } // 동영상 녹화한거 파일 경로
    private val videoPath :String by lazy {
        "${externalCacheDir?.absolutePath}/videoRecord.m4a"
    }
    private var file_path:String?=null

    private lateinit var mSurfaceViewHolder: SurfaceHolder
    private lateinit var mImageReader: ImageReader
    private lateinit var mCameraDevice: CameraDevice
    private lateinit var mPreviewBuilder: CaptureRequest.Builder
    private lateinit var mSession: CameraCaptureSession
    private var mHandler: Handler? = null

    private lateinit var mAccelerometer: Sensor
    private lateinit var mMagnetometer: Sensor
    private lateinit var mSensorManager: SensorManager

    private val deviceOrientation: DeviceOrientation by lazy { DeviceOrientation() }
    private var mHeight: Int = 0
    private var mWidth:Int = 0

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
    private var videoUri : Uri? = null // video 저장될 Uri
    private var mImageDimension: Size? = null
    private var mVideoDimension: Size? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idx=intent.getIntExtra("RECORD_IDX",0)
        title=intent.getStringExtra("RECORD_TITLE")
        singer=intent.getStringExtra("RECORD_SINGER")
        lyrics=intent.getStringExtra("RECORD_LYRICS")
        with=intent.getStringExtra("WITH")
        way=intent.getStringExtra("WAY")
        song_path= intent.getStringExtra("RECORD_SONG_PATH").toString()

        initSensor()
        initView()
        // 툴바 색깔 지정
        binding.toolbarRecord.setBackgroundColor(resources.getColor(R.color.dark_purple))

        // 제목
        binding.activityRecordTvTitle.text=title
        // 가수
        binding.activityRecordTvSinger.text=singer
        // 가사
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
                        intent.putExtra("USER_PATH",videoPath)
                        intent.putExtra("WITH",with)
                        intent.putExtra("WAY",way)
                        intent.putExtra("URI",videoUri)
                        Log.e("비디오액티비티","idx,file_path,recordingVideoFilePath,with,way"+
                        idx+" "+file_path+" "+recordingVideoFilePath+" "+with+" "+way)
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
            // 녹화 시작
            Record()

            binding.activityRecordBtnStart.visibility= View.GONE
            binding.activityRecordBtnPause.visibility=View.VISIBLE

        }
        // 중지버튼
        binding.activityRecordBtnPause.setOnClickListener {
            mediaPlayer.pause()

        }

    }
    // 사용자 비디오 녹화
    fun Record() {
        // 동영상 촬영을 위해 MediaRecorder 객체를 생성해준다
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP)
            setVideoEncodingBitRate(10000000)
            setVideoFrameRate(30)
            setOrientationHint(90)
            setOutputFile(recordingVideoFilePath) // 외부 캐시 디렉토리에 임시적으로 저장 ,위에 선언해둔 외부 캐시 FilePath 를 이용
            setVideoSize(mVideoDimension!!.width,mVideoDimension!!.height)
            prepare()
        }
        recorder!!.start()

        Toast.makeText(applicationContext, "녹화시작", Toast.LENGTH_SHORT).show()
    }

    private fun newVideoFileName() : String {
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.mp4"
    }

    // mediaRecorder 객체의 값을 설정해주는 메서드
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun setUpMediaRecorder() {
//        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
//        recorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
//        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//
//        var file = File(Environment.getExternalStorageDirectory().toString() + "/video${fileCount}.mp4")
//        this.file = file
//        recorder!!.setOutputFile(file)
//        recorder!!.setVideoEncodingBitRate(10000000)
//        recorder!!.setVideoFrameRate(30)
//        recorder!!.setVideoSize(videoDimension!!.width, videoDimension!!.height)
//        recorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//
//        val rotation = windowManager.defaultDisplay.rotation
//        recorder!!.setOrientationHint(ORIENTATIONS.get(rotation))
//
//        recorder!!.prepare()
//    }


    fun recordStop() {
        recorder?.run {
            stop()
            reset()
            release()

        }
        recorder=null
        mSession.close()

//        val videoFile = File (
//            File("${filesDir}/video").apply {
//                if(!this.exists()){
//                    this.mkdirs()
//                }
//            },
//            newVideoFileName()
//        )
//        videoUri = FileProvider.getUriForFile(
//            this,
//            "com.harimi.singtogether.fileprovider",
//            videoFile
//        )
//        video_path=videoFile.absolutePath

//        try {
//            recorder!!.stop()
//            recorder!!.reset()
//            recorder!!.release()
//
//
//            recorder=null
//
//            //mSession.close()
//
//        } catch (e : Exception) {
//            //exception 처리
//            e.printStackTrace()
//        }finally {
//            // 정상적이든 오류든 무조건 실행
//            file_path="${externalCacheDir?.absolutePath}/userVideo.mp4"
//        }

        //Toast.makeText(applicationContext, "녹음중지", Toast.LENGTH_SHORT).show()
        //Merge()
    }

    override fun onDestroy(){
        super.onDestroy()
        mediaPlayer.release()
    }


    private fun initSensor() {
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    private fun initView() {
        with(DisplayMetrics()){
            windowManager.defaultDisplay.getMetrics(this)
            mHeight = heightPixels
            mWidth = widthPixels
        }

        mSurfaceViewHolder = binding.surfaceView.holder
        mSurfaceViewHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                initCameraAndPreview()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mCameraDevice.close()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int,
                width: Int, height: Int
            ) {

            }
        })

        binding.btnConvert.setOnClickListener { switchCamera() }
    }

    // 카메라 전환
    private fun switchCamera() {
        when(mCameraId){
            CAMERA_BACK -> {
                mCameraId = CAMERA_FRONT
                mCameraDevice.close()
                openCamera()
            }
            else -> {
                mCameraId = CAMERA_BACK
                mCameraDevice.close()
                openCamera()
            }
        }
    }


    fun initCameraAndPreview() {
        val handlerThread = HandlerThread("CAMERA2")
        handlerThread.start()
        mHandler = Handler(handlerThread.looper)

        openCamera()
    }

    private fun openCamera() {
        try {
            val mCameraManager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val characteristics = mCameraManager.getCameraCharacteristics(mCameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            val largestPreviewSize = map!!.getOutputSizes(ImageFormat.JPEG)[0]
            setAspectRatioTextureView(largestPreviewSize.height, largestPreviewSize.width)

            mImageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            mVideoDimension = map.getOutputSizes(MediaRecorder::class.java)[0]

            mImageReader = ImageReader.newInstance(
                largestPreviewSize.width,
                largestPreviewSize.height,
                ImageFormat.JPEG,
                7
            )
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) return

            mCameraManager.openCamera(mCameraId, deviceStateCallback, mHandler)
        } catch (e: CameraAccessException) {
           Toast.makeText(this,"카메라를 열지 못했습니다.",Toast.LENGTH_SHORT).show()
        }
    }

    private val deviceStateCallback = object : CameraDevice.StateCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        override fun onOpened(camera: CameraDevice) {
            mCameraDevice = camera
            try {
                takePreview()
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        override fun onDisconnected(camera: CameraDevice) {
            mCameraDevice.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Toast.makeText(this@VideoActivity,"카메라를 열지 못했습니다.",Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(CameraAccessException::class)
    fun takePreview() {
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        mPreviewBuilder.addTarget(mSurfaceViewHolder.surface)
        mCameraDevice.createCaptureSession(
            listOf(mSurfaceViewHolder.surface, mImageReader.surface), mSessionPreviewStateCallback, mHandler
        )
    }

    private val mSessionPreviewStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            mSession = session
            try {
                // Key-Value 구조로 설정
                // 오토포커싱이 계속 동작
                mPreviewBuilder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                //필요할 경우 플래시가 자동으로 켜짐
                mPreviewBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                )
                mSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }

        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Toast.makeText(this@VideoActivity, "카메라 구성 실패", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        mSensorManager.registerListener(
            deviceOrientation.eventListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI
        )
        mSensorManager.registerListener(
            deviceOrientation.eventListener, mMagnetometer, SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(deviceOrientation.eventListener)
    }

    private fun setAspectRatioTextureView(ResolutionWidth: Int, ResolutionHeight: Int) {
        if (ResolutionWidth > ResolutionHeight) {
            val newWidth = mWidth
            val newHeight = mWidth * ResolutionWidth / ResolutionHeight
            updateTextureViewSize(newWidth, newHeight)

        } else {
            val newWidth = mWidth
            val newHeight = mWidth * ResolutionHeight / ResolutionWidth
            updateTextureViewSize(newWidth, newHeight)
        }

    }

    private fun updateTextureViewSize(viewWidth: Int, viewHeight: Int) {
        Log.d("ViewSize", "TextureView Width : $viewWidth TextureView Height : $viewHeight")
        binding.surfaceView.layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)

    }
}