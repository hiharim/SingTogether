package com.harimi.singtogether.sing

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.*
import android.media.*
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.harimi.singtogether.R
import com.harimi.singtogether.databinding.ActivityVideo3Binding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Video3Activity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var binding: ActivityVideo3Binding
    private var idx : Int? = null
    private var title : String? = null
    private var singer : String? = null
    private var with : String? = null
    private var way : String? = null
    private var lyrics : String? = null // 가사
    private lateinit var song_path : String
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var mSurfaceViewHolder: SurfaceHolder
    private lateinit var mImageReader: ImageReader
    private lateinit var mCameraDevice: CameraDevice
    private lateinit var mPreviewBuilder: CaptureRequest.Builder
    private var mSession: CameraCaptureSession ?=null
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
    private val recordingVideoFilePath :String by lazy {
        "${externalCacheDir?.absolutePath}/recordVideo3.mp4"
    } // 동영상 녹화한거 파일 경로

    private var file_path:String?=null
    private var  mRecorder : MediaRecorder?=null // 사용하지 않을 때는 메모리 해제 및 null 처리
    private var isRecording = false
    private var mVideoDimension: Size? = null
    var mOutputVideoFile : File ?=null
    lateinit var captureRequestBuilder :CaptureRequest.Builder
    private var previewSize : Size?=null
    private lateinit var backgroundHandlerThread: HandlerThread
    private lateinit var backgroundHandler: Handler

    private fun startBackgroundThread() {
        backgroundHandlerThread = HandlerThread("CameraVideoThread")
        backgroundHandlerThread.start()
        backgroundHandler = Handler(
            backgroundHandlerThread.looper)
    }

    private fun stopBackgroundThread() {
        backgroundHandlerThread.quitSafely()
        backgroundHandlerThread.join()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityVideo3Binding.inflate(layoutInflater)
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

        initSensor()
        initView()

        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(song_path)
        mediaPlayer.prepare()

        mSurfaceViewHolder = binding.surfaceView.getHolder()
        mSurfaceViewHolder!!.addCallback(this)
        mSurfaceViewHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

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

                        //mRecorder!!.stop()
                        //mRecorder!!.release()
                        //mRecorder = null
                        isRecording = false


                        val intent= Intent(applicationContext, AfterRecordActivity::class.java)
                        intent.putExtra("MR_IDX", idx)
                        intent.putExtra("FILE_PATH", file_path)
                        intent.putExtra("USER_PATH", recordingVideoFilePath)
                        intent.putExtra("WITH", with)
                        intent.putExtra("WAY", way)
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

    // 파일
    private fun createOutputFile(ext: String): File? {
        val tempFile: File
        val dir = File(cacheDir, "captures")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val c: Calendar = Calendar.getInstance()
        val filename = "ABC_" +
                c.get(Calendar.YEAR).toString() + "-" +
                (c.get(Calendar.MONTH) + 1).toString() + "-" +
                c.get(Calendar.DAY_OF_MONTH)
                    .toString() + "_" +
                c.get(Calendar.HOUR_OF_DAY) +
                c.get(Calendar.MINUTE) +
                c.get(Calendar.SECOND)
        tempFile = File(dir, filename + ext)
        return tempFile
    }


    fun startVideoRecorder() {
        if (isRecording) {
            mRecorder!!.stop()
            mRecorder!!.release()
            mRecorder = null
            //mCamera!!.lock()
            isRecording = false
            closeCameraPreviewSession()

        } else {
            runOnUiThread {
                mRecorder = MediaRecorder()
                //mCamera!!.unlock()
                //mRecorder!!.setCamera(mCamera)

                mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                mRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
                mRecorder!!.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH))
                //                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                //                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                //                    mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
                mRecorder!!.setOrientationHint(90)
                mRecorder!!.setVideoSize(previewSize!!.width,previewSize!!.height)
                mRecorder!!.setOutputFile("/sdcard/video3Test.mp4")
               // mRecorder!!.setPreviewDisplay(mSurfaceViewHolder!!.surface)
                //mRecorder!!.setOutputFile(recordingVideoFilePath)
                try {
                    mRecorder!!.prepare()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                //mRecorder!!.surface
                mRecorder!!.start()
                isRecording = true
                //startRecording()

            }
        }
    }

    fun startRecording() {
        val surfaceTexture : SurfaceTexture? = binding.surfaceViewRecord.surfaceTexture
        surfaceTexture?.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)
        val previewSurface: Surface = Surface(surfaceTexture)
        val recordingSurface = mRecorder!!.surface
        captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        captureRequestBuilder.addTarget(previewSurface)
        captureRequestBuilder.addTarget(recordingSurface)

        mCameraDevice.createCaptureSession(listOf(previewSurface, recordingSurface), captureStateVideoCallback, backgroundHandler)
    }

    val captureStateVideoCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) {

        }

        override fun onConfigured(session: CameraCaptureSession) {
            session.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler)
            mRecorder!!.start()
        }
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
                // surfaceView 처음 생성 될 때 발생하는 함수
                // 카메라와 SurfaceHolder 를 연결하고 카메라 preview 를 시작한다
                initCameraAndPreview()
            }

            // surfaceView 객체가 사라지게 되면 발생하는 함수
            // 카메라 리소스 반환
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mCameraDevice.close()
            }

            // 상태가 변경될때마다 발생하는 함수
            // surfaceView 에 맞게 카메라 preview 도 재설정한 후 다시 시작한다
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
                mCameraId= CAMERA_BACK
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
        Log.e("비디오액티비티", "openCamera() : openCamera()메서드가 호출되었음")
        try {
            // 카메라의 정보를 가져와서 cameraId 와 imageDimension 에 값을 할당하고, 카메라를 열어야 하기 때문에
            // CameraManager 객체를 가져온다
            val mCameraManager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            // 선택한 카메라의 특징을 알 수 있다
            val characteristics = mCameraManager.getCameraCharacteristics(mCameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            val largestPreviewSize = map!!.getOutputSizes(ImageFormat.JPEG)[0]
            setAspectRatioTextureView(largestPreviewSize.height, largestPreviewSize.width)
            mVideoDimension=map.getOutputSizes<MediaRecorder>(MediaRecorder::class.java)[0]

            previewSize =  characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!

            // 원하는 크기와 형식의 이미지를 받아올 수 있는 ImageReader 객체를 생성
            mImageReader = ImageReader.newInstance(
                largestPreviewSize.width,
                largestPreviewSize.height,
                ImageFormat.JPEG,
                7
            )
            // VideoRecording 에 사용할 Size 값을 map 에서 가져와 videoDimension 에 할당해준다
            //videoDimension = map!!.getOutputSizes<MediaRecorder>(MediaRecorder::class.java)[0]


            Log.e(
                "  mImageReader",
                " width:" + largestPreviewSize.width + " height:" + largestPreviewSize.height
            )
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) return

            // CameraManager.openCamera() 메서드를 이용해 인자로 넘겨준 cameraId 의 카메라를 실행한다
            //cameraDevice객체를 생성하는데 다소 시간이 소요되므로 콜백메소드를 통해 생성한다
            // 이때, deviceStateCallback 은 카메라를 실행할때 호출되는 콜백메서드이며, cameraDevice 에 값을 할달해주고,
            // 카메라 미리보기를 생성한다
            mCameraManager.openCamera(mCameraId, deviceStateCallback, mHandler)

        } catch (e: CameraAccessException) {
           Toast.makeText(this, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // openCamera() 메서드에서 CameraManager.openCamera() 를 실행할때 인자로 넘겨주어야하는 콜백메서드
    // 카메라가 제대로 열렸으면, cameraDevice 에 값을 할당해주고, 카메라 미리보기를 생성한다
    private val deviceStateCallback = object : CameraDevice.StateCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        override fun onOpened(camera: CameraDevice) {
            Log.e("비디오액티비티", "stateCallback : onOpened")
            // VideoActivity 의 cameraDevice 에 값을 할당해주고, 카메라 미리보기를 시작한다
            // 나중에 cameraDevice 리소스를 해지할때 해당 cameraDevice 객체의 참조가 필요하므로,
            // 인자로 들어온 camera 값을 전역변수 cameraDevice 에 넣어 준다
            mCameraDevice = camera
            try {
                // takePreview() 메서드로 카메라 미리보기를 생성해준다
                takePreview()
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d("메인액티비티", "stateCallback : onDisconnected")
            // 연결이 해제되면 cameraDevice 를 닫아준다
            mCameraDevice.close()

        }

        override fun onError(camera: CameraDevice, error: Int) {
            Toast.makeText(this@Video3Activity, "카메라를 열지 못했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(CameraAccessException::class)
    fun takePreview() {
        //카메라 장치에서 미리보기를 가져올 때, 어떤식으로 보여달라고 할 지 request 를 보내게 되는데,
        // 이 request 를 만들어주는 것이 바로 captureRequestBuilder 이다.
        // 여기서는 미리보기 화면을 요청할 것이므로, 파라미터에 CameraDevice.TEMPLATE_PREVIEW 를 넣어준다.
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        mPreviewBuilder.addTarget(mSurfaceViewHolder.surface)
        mCameraDevice.createCaptureSession(
            listOf(mSurfaceViewHolder.surface, mImageReader.surface),
            mSessionPreviewStateCallback,
            mHandler
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
                mSession!!.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler)

            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }

        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Toast.makeText(this@Video3Activity, "카메라 구성 실패", Toast.LENGTH_SHORT).show()
        }
    }

    //기존의 카메라 미리보기 세션을 닫아주는 메서드
    private fun closeCameraPreviewSession() {
        if (mSession != null) {
            mSession !!.close()
            mSession  = null
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
        Log.e("ViewSize", "TextureView Width : $viewWidth TextureView Height : $viewHeight")
        binding.surfaceView.layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)

    }

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }


}