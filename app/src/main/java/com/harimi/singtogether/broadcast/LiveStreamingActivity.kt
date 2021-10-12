package com.harimi.singtogether.broadcast





import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.hardware.Camera
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.AudioManager
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build

import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.harimi.singtogether.*
import com.harimi.singtogether.Data.LiveStreamingViewerListData
import com.harimi.singtogether.Data.LocalChattingData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R.*
import com.harimi.singtogether.adapter.LiveStreamingViewerListAdapter
import com.harimi.singtogether.adapter.LocalChattingAdapter
import com.harimi.singtogether.broadcast.SignalingClient.Companion.get
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.*

import org.webrtc.audio.JavaAudioDeviceModule
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * 실시간 방송하는 액티비티 화면
 * */
class LiveStreamingActivity : AppCompatActivity() , SignalingClient.Callback{
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private var roomIdx :String? =null
    private var roomTitle :String? =null
    private var thumbnail :String? =null

    var audioConstraints: MediaConstraints? = null
    var audioSource: AudioSource? = null
    var localAudioTrack: AudioTrack? = null
    var eglBaseContext: EglBase.Context? = null
    var peerConnectionFactory: PeerConnectionFactory? = null
    var mediaStream: MediaStream? = null
    var iceServers: MutableList<PeerConnection.IceServer>? = null
    var peerConnection: PeerConnection? = null
    var peerConnectionMap: HashMap<String?, PeerConnection?>? = null
    private val TAG = "STREAMING_ACTIVITY"
    var videoCapturer: VideoCapturer? = null
    var videoSource: VideoSource? = null
    var videoTrack: VideoTrack? = null



    private var mScreenDensity = 0
    private var mediaProjectionManager: MediaProjectionManager? = null
    private val DISPLAY_WIDTH = 720
    private val DISPLAY_HEIGHT = 1280
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mMediaProjectionCallback: MediaProjectionCallback? = null
    private var iv_videoRecord: ToggleButton? = null
    private lateinit var localVideoView :VideoView
    private var mediaRecorder: MediaRecorder? = null
    private lateinit var layoutRoot: RelativeLayout
    private var videoUri = ""

    companion object {

        private const val REQUEST_CODE_MediaProjection = 1001
        private val ORIENTATIONS = SparseIntArray()
        private const val REQUEST_PERMISSIONS = 10

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

    private lateinit var activity_streaming_tv_count :TextView
    private lateinit var activity_streaming_btn_close :ImageView
    private lateinit var sv_searchViewer :SearchView
    private lateinit var activity_streaming_btn_switch_cam_backCamera :ImageButton
    private lateinit var et_chattingInputText :EditText
    private lateinit var btn_sendInputText :Button
    private lateinit var activity_streaming_btn_chat :ImageButton
    private lateinit var activity_streaming_tv_time :TextView
    private lateinit var activity_streaming_btn_viewerList : ImageButton
    private lateinit var StreamingDrawerLayout : DrawerLayout
//    private lateinit var iv_videoRecord : ImageButton

    /////DrawerLayout 리사이클러뷰,데이터리스트, 어댑터
    private lateinit var rv_streamingViewerList : RecyclerView
    private val liveStreamingViewerList: ArrayList<LiveStreamingViewerListData> = ArrayList()
    private val tempLiveStreamingViewerList: ArrayList<LiveStreamingViewerListData> = ArrayList()
    private lateinit var liveStreamingViewerAdapter: LiveStreamingViewerListAdapter
    private lateinit var layout_notify: LinearLayout


    private var messageArea : Boolean = false
    private var viewer : String ? ="0"
    private var timerTask: Timer? = null //타이머
    private var time = 0 //
    private val localChattingList: ArrayList<LocalChattingData> = ArrayList()
    private lateinit var rv_chattingRecyclerView : RecyclerView
    private lateinit var localChattingAdapter: LocalChattingAdapter
    private var localStreamingView: SurfaceViewRenderer? = null

    ////스트리밍 녹화

    private var isRecording = false
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mCamera: Camera? = null
    private lateinit var fileName : String // 서버로 보낼 비디오 파일 이름
    private var videoFile : File ?=null // 녹화된 비디오파일

    private val recordingVideoFilePath :String by lazy {
        "${externalCacheDir?.absolutePath}/recordVideos28.mp4"
    }

    ////갤럭시 바텀 네비게이션바 없애기
    private var decorView: View? = null
    private var uiOption = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_live_streaming)

        initView()


    }

    fun initView(){

        //바텀 네비게이션 안보이게 하기
        decorView = window.decorView
        uiOption = window.decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) uiOption =
            uiOption or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) uiOption =
            uiOption or View.SYSTEM_UI_FLAG_FULLSCREEN
        uiOption =
            uiOption or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView!!.setSystemUiVisibility(uiOption)


        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        mScreenDensity = metrics.densityDpi

        mediaRecorder = MediaRecorder()
        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager



        val getIntent = intent
        roomIdx = getIntent.getStringExtra("roomIdx")
        roomTitle = getIntent.getStringExtra("roomTitle")
        thumbnail = getIntent.getStringExtra("thumbnail")

        initRetrofit()

        localVideoView = findViewById<VideoView>(id.localVideoView)
        layoutRoot = findViewById<RelativeLayout>(id.layoutRoot)
        iv_videoRecord = findViewById<ToggleButton>(id.iv_videoRecord)

//        iv_videoRecord = findViewById<ImageButton>(id.iv_videoRecord)
//        localSurfaceView = findViewById<SurfaceView>(id.localSurfaceView)

        sv_searchViewer = findViewById<SearchView>(id.sv_searchViewer)
        StreamingDrawerLayout = findViewById<DrawerLayout>(id.StreamingDrawerLayout)
        layout_notify = findViewById<LinearLayout>(id.layout_notify)
        activity_streaming_btn_viewerList = findViewById<ImageButton>(id.activity_streaming_btn_viewerList)
        activity_streaming_tv_count = findViewById<TextView>(id.activity_streaming_tv_count) //방송 시청자
        activity_streaming_btn_close = findViewById<ImageView>(id.activity_streaming_btn_close)// 나가기 버튼
        activity_streaming_btn_switch_cam_backCamera =findViewById(id.activity_streaming_btn_switch_cam_backCamera)//  카메라 전환 버튼
        et_chattingInputText = findViewById<EditText>(id.et_chattingInputText)
        btn_sendInputText = findViewById<Button>(id.btn_sendInputText)
        activity_streaming_btn_chat = findViewById<ImageButton>(id.activity_streaming_btn_chat) ////채팅창 visible 설정
        activity_streaming_tv_time = findViewById<TextView>(id.activity_streaming_tv_time) // 타이머

        ////스트리밍 채팅 리사이클러뷰
        rv_chattingRecyclerView = findViewById(id.rv_chattingRecyclerView)
        rv_chattingRecyclerView.layoutManager = LinearLayoutManager(this)
        rv_chattingRecyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        localChattingAdapter = LocalChattingAdapter(localChattingList, this)
        rv_chattingRecyclerView.adapter = localChattingAdapter



        ////현재들어와있는 사람들리스트 리사이클러뷰
        rv_streamingViewerList = findViewById(id.rv_streamingViewerList)
        rv_streamingViewerList.layoutManager = LinearLayoutManager(this)
        rv_streamingViewerList.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        liveStreamingViewerAdapter = LiveStreamingViewerListAdapter(
            tempLiveStreamingViewerList,
            this
        )
        rv_streamingViewerList.adapter = liveStreamingViewerAdapter

        ////초기 뷰어 리스트 셋팅
        viewerListSetting()

        activity_streaming_tv_count.text = viewer // 초기 시청자 셋팅

        //스턴서버를통하여서 ICE 프레임워크를 사용하여서 P2P연결을하는데 최적의 경로를 찾아줌
        peerConnectionMap = HashMap()
        iceServers = ArrayList()
        Log.d(TAG, "iceServers_Add")
        iceServers!!.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())



        eglBaseContext = EglBase.create().eglBaseContext
        // create PeerConnectionFactory
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions
                .builder(this)
                .createInitializationOptions()
        )
        val options = PeerConnectionFactory.Options()
        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(eglBaseContext, true, true)
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(eglBaseContext)


        //오디오 모듈을 집어넣는다 .
        val audioDeviceModule = JavaAudioDeviceModule.builder(applicationContext)
            .setUseHardwareAcousticEchoCanceler(false)
            .setUseHardwareNoiseSuppressor(false)
            .createAudioDeviceModule()


        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setAudioDeviceModule(audioDeviceModule)
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .createPeerConnectionFactory()


        //비디오 트랙 채널과 소스
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)
        videoCapturer = createCameraCapturer(true) ///카메라를 정면선택할지 후면선택할지 선택
        videoSource = peerConnectionFactory!!.createVideoSource(videoCapturer!!.isScreencast)
        videoCapturer!!.initialize(
            surfaceTextureHelper,
            applicationContext,
            videoSource?.capturerObserver
        )
        videoCapturer!!.startCapture(480, 640, 30)
        videoTrack = peerConnectionFactory!!.createVideoTrack("100", videoSource)

        localStreamingView = findViewById(id.localStreamingView)
        localStreamingView!!.setMirror(true)
        localStreamingView!!.init(eglBaseContext, null)


        //오디오 트랙 채널과 소스
        audioConstraints = MediaConstraints()

        audioSource = peerConnectionFactory!!.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory!!.createAudioTrack("101", audioSource)
        localAudioTrack!!.setVolume(10.0)

        //로컬뷰
        videoTrack!!.addSink(localStreamingView)
        mediaStream = peerConnectionFactory?.createLocalMediaStream("mediaStream")
        //미디어 스트림에 비디오트랙 넣기
        mediaStream!!.addTrack(videoTrack)
        //미디어 스트림에 오디오 트랙에 넣기
        mediaStream!!.addTrack(localAudioTrack)

        // 오디오 스피커 모드로 설정하기
        val am: AudioManager
        am = getSystemService(AUDIO_SERVICE) as AudioManager
        am.isSpeakerphoneOn = true
        Log.d("PeerHashMap", " $peerConnectionMap")
        get()!!.init(this, roomIdx)


        ///스트리밍 녹화
        val builder = AlertDialog.Builder(this)
        builder.setTitle("녹화")
        builder.setMessage("방송 녹화를 하시겠습니까?")

        builder.setPositiveButton("네") { dialog, which ->
            iv_videoRecord!!.performClick()
            isRecording = true
            iv_videoRecord!!.visibility = View.GONE
        }
        builder.setNegativeButton("아니요") { dialog, which ->
        }
        builder.show()




        //시청자 찾기 , 실시간 텍스쳐 검사
        sv_searchViewer.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("NOT YET")
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                tempLiveStreamingViewerList.clear()
                val searchText = newText!!.toLowerCase(Locale.getDefault())
                if (searchText.isNotEmpty()) {
                    liveStreamingViewerList.forEach {
                        if (it.nickName.toLowerCase(Locale.getDefault()).contains(searchText)) {
                            tempLiveStreamingViewerList.add(it)
                        }
                    }
                    liveStreamingViewerAdapter.notifyDataSetChanged()
                } else {
                    tempLiveStreamingViewerList.clear()
                    tempLiveStreamingViewerList.addAll(liveStreamingViewerList)
                    liveStreamingViewerAdapter.notifyDataSetChanged()
                }
                return false
            }
        })

        ////녹화하기
        iv_videoRecord!!.setOnClickListener(View.OnClickListener { v ->
            isRecording = true
            if ((ContextCompat.checkSelfPermission(this@LiveStreamingActivity, Manifest.permission.RECORD_AUDIO)
                        + ContextCompat.checkSelfPermission(this@LiveStreamingActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        + ContextCompat.checkSelfPermission(this@LiveStreamingActivity, Manifest.permission.FOREGROUND_SERVICE))
                != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@LiveStreamingActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this@LiveStreamingActivity, Manifest.permission.RECORD_AUDIO)
                    ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this@LiveStreamingActivity, Manifest.permission.FOREGROUND_SERVICE)) {
                    iv_videoRecord!!.setChecked(true)

                    Snackbar.make(layoutRoot!!, "Permission", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Enable") {
                            ActivityCompat.requestPermissions(this@LiveStreamingActivity, arrayOf(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.FOREGROUND_SERVICE
                            ), REQUEST_PERMISSIONS)
                        }.show()
                } else {
                    ActivityCompat.requestPermissions(this@LiveStreamingActivity, arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.FOREGROUND_SERVICE
                    ), REQUEST_PERMISSIONS)
                }
            } else {
                toggleScreenShare(v)
            }
        })




    //방송시간 나타내기
        timerTask = kotlin.concurrent.timer(period = 1000) {
            time++ // period=10으로 0.01초마다 time를 1씩 증가하게 됩니다
            var min = time /60
            var hour = min / 60
            runOnUiThread {
                if (min <=9){
                    activity_streaming_tv_time.text = "$hour: 0$min"
                }else{
                    activity_streaming_tv_time.text = "$hour: $min"
                }
            }
        }


        ///채팅 VISIBLE 설정
        activity_streaming_btn_chat.setOnClickListener {
            if(messageArea){
                messageArea = false
                rv_chattingRecyclerView.visibility  = View.VISIBLE
                et_chattingInputText.visibility =View.VISIBLE
                btn_sendInputText.visibility =View.VISIBLE

            }else{
                messageArea = true
                rv_chattingRecyclerView.visibility  = View.INVISIBLE
                et_chattingInputText.visibility =View.INVISIBLE
                btn_sendInputText.visibility =View.INVISIBLE
            }
        }

        ///채팅 보내기 버튼 눌렀을 때
        btn_sendInputText.setOnClickListener {
            val chattingText = et_chattingInputText.text.toString()

            et_chattingInputText.setText("")
            get()!!.chattingInput(
                roomIdx!!,
                LoginActivity.user_info.loginUserNickname,
                chattingText,
                LoginActivity.user_info.loginUserProfile
            )
        }


        ////카메라 전환버튼 눌렀을 때
        activity_streaming_btn_switch_cam_backCamera.setOnClickListener {
            switchCamera()
        }

        ////나가기 버튼 눌렀을 때
        activity_streaming_btn_close.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("종료")
            builder.setMessage("방송을 종료하시겠습니까?")

            builder.setPositiveButton("네") { dialog, which ->
                finishActivity()
            }
            builder.setNegativeButton("아니요") { dialog, which ->
            }
            builder.show()
        }

        ////시청자 보기
        activity_streaming_btn_viewerList.setOnClickListener {
            clickViewerList(it)
        }
    }


    ///////화면녹화하기
    private fun initMediaProjection() {
        Log.v(TAG, "initView")
        val intent = Intent(this@LiveStreamingActivity, MyService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
            startMediaProjection()
        } else {
            startService(intent)
            startMediaProjection()
        }

    }

    private fun startMediaProjection() {
        Log.v(TAG, "startMediaProjection")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivityForResult(mediaProjectionManager!!.createScreenCaptureIntent(), REQUEST_CODE_MediaProjection)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                Log.v(TAG, "REQUEST_PERMISSIONS")
                if (grantResults.size > 0 && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    toggleScreenShare(iv_videoRecord)
                } else {
                    iv_videoRecord!!.isChecked = false
                    Snackbar.make(layoutRoot!!, "Permission", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Enable") {
                            ActivityCompat.requestPermissions(this@LiveStreamingActivity, arrayOf(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.FOREGROUND_SERVICE
                            ), REQUEST_PERMISSIONS)
                        }.show()
                }
                return
            }

        }
    }
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_CODE_MediaProjection && resultCode == RESULT_OK) {
            Log.v(TAG, "onActivityResult")
            mMediaProjectionCallback = MediaProjectionCallback()
            mMediaProjection = mediaProjectionManager!!.getMediaProjection(resultCode, data!!)
            mMediaProjection!!.registerCallback(mMediaProjectionCallback, null)
            mVirtualDisplay = createVirtualDisplay()
            mediaRecorder!!.start()
        }
        super.onActivityResult(requestCode, resultCode, data)

    }
    private fun createVirtualDisplay(): VirtualDisplay {
        Log.v(TAG, "createVirtualDisplay")
        return mMediaProjection!!.createVirtualDisplay("MainActivity",
            DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder!!.surface, null /*Callbacks*/, null /*Handler*/
        )
    }

    private fun toggleScreenShare(v: View?) {
        if ((v as ToggleButton?)!!.isChecked) {
            Log.v(TAG, "Start Recording")
            initMediaProjection()
            initRecorder()

        } else {
            mediaRecorder!!.stop()
            mediaRecorder!!.reset()
            Log.v(TAG, "Stopping Recording")
            stopRecordScreen()
//            localVideoView!!.visibility = View.VISIBLE
//            localVideoView!!.setVideoURI(Uri.parse(videoUri))
//            localVideoView!!.start()
        }
    }

    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            Log.v(TAG, "MediaProjectionCallback")
            if (iv_videoRecord!!.isChecked) {
                iv_videoRecord!!.isChecked = false
                mediaRecorder!!.stop()
                mediaRecorder!!.reset()
                Log.v(TAG, "Recording Stopped")
            }
            mMediaProjection = null
            stopRecordScreen()
        }
    }
    private fun stopRecordScreen() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay!!.release()
        destroyMediaProjection()
    }
    private fun destroyMediaProjection() {
        Log.v(TAG, "destroyMediaProjection")
        if (mMediaProjection != null) {
            mMediaProjection!!.unregisterCallback(mMediaProjectionCallback)
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
        Log.i(TAG, "MediaProjection Stopped")
    }

    private fun initRecorder() {
        Log.v(TAG, "initRecorder")
        try {
            mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            videoUri = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + StringBuilder("/EDMTRecord_").append(
                SimpleDateFormat("dd-MM-yyyy-mm-ss")
                    .format(Date())).append(".mp4").toString()
            mediaRecorder!!.setOutputFile(videoUri)
            mediaRecorder!!.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            mediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder!!.setVideoEncodingBitRate(512 * 1000)
            mediaRecorder!!.setVideoFrameRate(30)
            val rotation = windowManager.defaultDisplay.rotation
            val orientation = ORIENTATIONS[rotation + 90]
            mediaRecorder!!.setOrientationHint(orientation)
            mediaRecorder!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun uploadVideo() {
        Log.d(TAG, "업로드")

        mediaRecorder!!.stop()
        mediaRecorder!!.reset()
        Log.v(TAG, "Stopping Recording")
        stopRecordScreen()

        val uploadTime : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        fileName = "$uploadTime.mp4"
        var email= LoginActivity.user_info.loginUserEmail
        var nickname= LoginActivity.user_info.loginUserNickname
        var profile =LoginActivity.user_info.loginUserProfile
        Log.e(TAG, "profile  " +profile.toString())


        videoFile = File(videoUri)
        var requestBody : RequestBody = RequestBody.create(
            MediaType.parse("multipart/form-data"),
            videoFile
        )
        var body : MultipartBody.Part=
            MultipartBody.Part.createFormData("uploaded_file", fileName, requestBody)
        retrofitService.requestUploadReplayVideo(
            email,
            nickname,
            profile,
            roomTitle!!,
            thumbnail!!,
            body
        ).enqueue(object : Callback<String> {
            // 통신에 성공한 경우
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {

                    Log.e(TAG, "videoUpload  " + response.body().toString())
//                        val jsonObject = JSONObject(response.body().toString())
                } else {

                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(TAG, "통신실패")
            }
        })
    }
    //////////////////



    private fun initRetrofit(){
        retrofit=RetrofitClient.getInstance()
        retrofitService=retrofit.create(RetrofitService::class.java)
    }

    ///현재 들어와있는 시청자 보기
    fun clickViewerList(view: View?) {
        openDrawer(StreamingDrawerLayout)
    }
    //네비게이션 Drawer 보기
    fun openDrawer(drawerLayout: DrawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START)
    }
    ///네비게이션 닫기
    fun closeDrawer(drawerLayout: DrawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            //열려있으면 닫는다.
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }



    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        closeDrawer(StreamingDrawerLayout)
    }
    @Synchronized
    private fun getOrCreatePeerConnection(socketId: String?): PeerConnection? {
        Log.d(TAG, "getOrCreatePeerConnection")
        peerConnection = peerConnectionMap!![socketId]
        if (peerConnection != null) {
            return peerConnection
        }
        peerConnection = peerConnectionFactory!!.createPeerConnection(
            iceServers,
            object : PeerConnectionAdapter("PC:$socketId") {
                override fun onIceCandidate(iceCandidate: IceCandidate) {
                    super.onIceCandidate(iceCandidate)
                    Log.d(TAG, "onIceCandidate")
                    get()!!.sendIceCandidate(iceCandidate, socketId!!)

                }

                override fun onAddStream(mediaStream: MediaStream) {
                    super.onAddStream(mediaStream)
                    Log.d(TAG, "getOronAddStreamCreatePeerConnection")
//                val remoteVideoTrack = mediaStream.videoTracks[0]
//                    runOnUiThread {
//
//                    }
                }
            })
        peerConnection!!.addStream(mediaStream)
        peerConnectionMap!![socketId] = peerConnection
        return peerConnection
    }


    override fun onCreateRoom() {
        Log.d(TAG, "onCreateRoom")
    }

    ////시청자가 들어왔을 때 peer 연결
    override fun onPeerJoined(socketId: String?) {
        Log.d(TAG, "onPeerJoined " + socketId)
        val peerConnection = getOrCreatePeerConnection(socketId)
        peerConnection!!.createOffer(object : SdpAdapter("createOfferSdp:$socketId") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)

                var addViewer = Integer.parseInt(viewer!!)
                addViewer++
                viewer = addViewer.toString()
                activity_streaming_tv_count.text = viewer
                Log.d(TAG, "onAddStream" + viewer)
                var getViewer = activity_streaming_tv_count.text.toString()
                get()!!.getLiveStreamingViewer(roomIdx!!, getViewer)

                peerConnection.setLocalDescription(
                    SdpAdapter("setLocalSdp:$socketId"),
                    sessionDescription
                )
                get()!!.sendSessionDescription(sessionDescription, socketId!!)
            }
        }, MediaConstraints())
    }

    override fun onSelfJoined(userData: String?) {
        Log.d(TAG, "onSelfJoined" + userData)
    }

    ////메세지 왔을 때
    override fun onGetMessage(message: String?) {
        Log.d(TAG, "onGetMessage" + message)
            val jsonArray = JSONArray(message.toString())
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val chattingText = jsonObject.getString("inputText")
                val nickName = jsonObject.getString("nickName")
                val profile = jsonObject.getString("profile")
                val localchattingdata = LocalChattingData(nickName, chattingText, profile)
                    runOnUiThread {
                        localChattingList.add(localchattingdata)
                        localChattingAdapter.notifyDataSetChanged()
                        rv_chattingRecyclerView.scrollToPosition(localChattingList.size - 1)
                    }

            }

    }
    override fun onGetViewer(message: String?) {
        Log.d(TAG, "onGetViewer")
    }

    ///시청자가 들어왔을 때
    override fun addViewerList(message: String?) {
        Log.d(TAG, "addViewerList" + message)
        val jsonArray = JSONArray(message.toString())
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val socketId = jsonObject.getString("socketId")
            val nickName = jsonObject.getString("nickName")
            val profile = jsonObject.getString("profile")
            val userId = jsonObject.getString("userId")
            val liveStreamingViewerData= LiveStreamingViewerListData(
                nickName,
                profile,
                socketId,
                userId
            )
            runOnUiThread {
                tempLiveStreamingViewerList.add(liveStreamingViewerData)
                liveStreamingViewerList.add(liveStreamingViewerData)
                liveStreamingViewerAdapter.notifyDataSetChanged()
                viewerListSetting()
            }

        }
    }

    override fun onLiveStreamingFinish() {
        Log.d(TAG, "onLiveStreamingFinish")
    }

    ////시청자가 나갔을 때 함수
    override fun onOutViewer(message: String?) {
        Log.d(TAG, "onOutViewer")
        var outViewer = Integer.parseInt(viewer!!)
        outViewer--
        viewer = outViewer.toString()
        activity_streaming_tv_count.text = viewer
        get()!!.getLiveStreamingViewer(roomIdx!!, viewer.toString())
        val jsonArray = JSONArray(message.toString())
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val userId = jsonObject.getString("userId")
            for (i in 0 until liveStreamingViewerList.size) {
                if (liveStreamingViewerList.get(i).userId.equals(userId)){
                    runOnUiThread {
                        liveStreamingViewerList.removeAt(i)
                        liveStreamingViewerAdapter.notifyDataSetChanged()
                        viewerListSetting()
                        return@runOnUiThread
                    }
                }
            }
        }
    }

    override fun onViewerOutOfHere(message: String?) {
        Log.d(TAG, "onViewerOutOfHere")
    }

    override fun onPeerLeave(msg: String?) {
        Log.d(TAG, "onPeerLeave")
    }

    override fun onOfferReceived(data: JSONObject?) {
        Log.d(TAG, "onOfferReceived" + data.toString())
        runOnUiThread {
            val socketId = data!!.optString("from")
            val peerConnection = getOrCreatePeerConnection(socketId)
            peerConnection!!.setRemoteDescription(
                SdpAdapter("setRemoteSdp:$socketId"),
                SessionDescription(SessionDescription.Type.OFFER, data.optString("sdp"))
            )
            peerConnection.createAnswer(object : SdpAdapter("localAnswerSdp") {
                override fun onCreateSuccess(sdp: SessionDescription) {
                    super.onCreateSuccess(sdp)
                    peerConnectionMap!![socketId]!!.setLocalDescription(
                        SdpAdapter("setLocalSdp:$socketId"),
                        sdp
                    )
                    get()!!.sendSessionDescription(sdp, socketId)
                }
            }, MediaConstraints())
        }
    }

    override fun onAnswerReceived(data: JSONObject?) {
        Log.d(TAG, "onAnswerReceived" + data.toString())
        val socketId = data!!.optString("from")
        val peerConnection = getOrCreatePeerConnection(socketId)
        peerConnection!!.setRemoteDescription(
            SdpAdapter("setRemoteSdp:$socketId"),
            SessionDescription(SessionDescription.Type.ANSWER, data.optString("sdp"))
        )
    }

    override fun onIceCandidateReceived(data: JSONObject?) {
        Log.d(TAG, "onIceCandidateReceived" + data.toString())
        val socketId = data!!.optString("from")
        val peerConnection = getOrCreatePeerConnection(socketId)
        peerConnection!!.addIceCandidate(
            IceCandidate(
                data.optString("id"),
                data.optInt("label"),
                data.optString("candidate")
            )
        )
    }

    /////viewer 리스트 셋팅
    fun viewerListSetting(){
        if (liveStreamingViewerList.size ==0 ){
            layout_notify.visibility = View.VISIBLE
            rv_streamingViewerList.visibility = View.GONE
        }else{
            layout_notify.visibility = View.GONE
            rv_streamingViewerList.visibility = View.VISIBLE
        }
    }


    ////화면을 나가기 전에 db에 있는 파일을 지워준다 .
    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
        timerTask = null //타이머
        time = 0 //

    }

    //Peer간의 연결을 끊어주고 비디오 백그라운드에서 돌고있는 Render를 종료한다
    //localView release시켜줌
    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        get()!!.destroy()
        if (peerConnection == null) {
            videoCapturer!!.dispose()

        } else {
            peerConnection!!.dispose()
            videoCapturer!!.dispose()
            // 더 이상 카메라 eglRender가 돌아가지않도록 release ;
        }
        localStreamingView!!.release() // 더 이상 카메라 eglRender가 돌아가지않도록 release ;
        destroyMediaProjection()

    }

    //디바이스의 카메라 설정
    private fun createCameraCapturer(isFront: Boolean): VideoCapturer? {
        Log.d(TAG, "createCameraCapturer")
        val enumerator = Camera1Enumerator(false)
        val deviceNames = enumerator.deviceNames

        for (deviceName in deviceNames) {
            if (
                if (isFront) enumerator.isFrontFacing(deviceName)
                else enumerator.isBackFacing(
                    deviceName
                )) {
                val videoCapturer: VideoCapturer? = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    private fun switchCamera() {
        if (videoCapturer != null) {
            if (videoCapturer is CameraVideoCapturer) {
                val cameraVideoCapturer = videoCapturer as CameraVideoCapturer
                cameraVideoCapturer.switchCamera(null)
            } else {
            }
        }
    }

    private fun finishActivity() {
        retrofitService.requestFinishLiveStreamingPost(roomIdx!!)
            .enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.isSuccessful) {
//                        val jsonObject = JSONObject(response.body().toString())
                        Toast.makeText(applicationContext, "방송을 종료합니다", Toast.LENGTH_SHORT).show()
                        get()!!.liveStreamingFinish(roomIdx!!)

                        if (isRecording){
                            val builder = AlertDialog.Builder(this@LiveStreamingActivity)
                            builder.setTitle("다시보기 업로드")
                            builder.setMessage("업로드 하시겠습니까?")
                            builder.setPositiveButton("네") { dialog, which ->
                                uploadVideo()
                                finish()
                            }
                            builder.setNegativeButton("아니요") { dialog, which ->
                                finish()
                            }
                            builder.show()
                        }else{
                            finish()
                        }


                    } else {
                        Log.e("onResponse", "실패 : " + response.errorBody())
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d(
                        "실패:", "Failed API call with call: " + call +
                                " + exception: " + t
                    )
                }
            })
    }



    /////백버튼 눌렀을 때
    override fun onBackPressed() {
//        super.onBackPressed() ///오버라이드 하기위해선 super 받으면 안됨 .
        val builder = AlertDialog.Builder(this)
        builder.setTitle("종료")
        builder.setMessage("방송을 종료하시겠습니까?")
        builder.setPositiveButton("네") { dialog, which ->
            finishActivity()
        }
        builder.setNegativeButton("아니요") { dialog, which ->
        }
        builder.show()
    }

}