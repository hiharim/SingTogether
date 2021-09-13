package com.harimi.singtogether.broadcast

import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.*
import com.harimi.singtogether.Data.LiveStreamingViewerListData
import com.harimi.singtogether.Data.LocalChattingData
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R.*
import com.harimi.singtogether.adapter.LiveStreamingViewerListAdapter
import com.harimi.singtogether.adapter.LocalChattingAdapter
import com.harimi.singtogether.broadcast.SignalingClient.Companion.get
import org.json.JSONArray
import org.json.JSONObject
import org.webrtc.*
import org.webrtc.audio.JavaAudioDeviceModule
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*

/**
 * 실시간 방송하는 액티비티 화면
 * */
class LiveStreamingActivity : AppCompatActivity() , SignalingClient.Callback{
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService
    private var roomIdx :String? =null

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

    private lateinit var activity_streaming_tv_count :TextView
    private lateinit var activity_streaming_btn_close :ImageView
    private lateinit var activity_streaming_btn_switch_cam_backCamera :ImageButton
    private lateinit var et_chattingInputText :EditText
    private lateinit var btn_sendInputText :Button
    private lateinit var activity_streaming_btn_chat :ImageButton
    private lateinit var activity_streaming_tv_time :TextView
    private lateinit var activity_streaming_btn_viewerList : ImageButton
    private lateinit var StreamingDrawerLayout : DrawerLayout
    /////DrawerLayout 리사이클러뷰,데이터리스트, 어댑터
    private lateinit var rv_streamingViewerList : RecyclerView
    private val liveStreamingViewerList: ArrayList<LiveStreamingViewerListData> = ArrayList()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_live_streaming)

        val getIntent = intent
        roomIdx = getIntent.getStringExtra("roomIdx")
        Log.d(TAG, " $roomIdx")

        initView()

    }




    fun initView(){

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
        liveStreamingViewerAdapter = LiveStreamingViewerListAdapter(liveStreamingViewerList, this)
        rv_streamingViewerList.adapter = liveStreamingViewerAdapter

        ////초기 뷰어 리스트 셋팅
        viewerListSetting()

        activity_streaming_tv_count.text = viewer // 초기 시청자 셋팅
        peerConnectionMap = HashMap()
        iceServers = ArrayList()
        iceServers!!.add(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )
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
                Toast.makeText(
                    applicationContext,
                    "방송을 종료합니다", Toast.LENGTH_SHORT
                ).show()

                get()!!.liveStreamingFinish(roomIdx!!)
                finish()
            }
            builder.setNegativeButton("아니요") { dialog, which ->
            }
            builder.show()
        }

        activity_streaming_btn_viewerList.setOnClickListener {
            clickViewerList(it)
        }
    }



    fun clickViewerList(view: View?) {
        openDrawer(StreamingDrawerLayout)
    }

    fun openDrawer(drawerLayout: DrawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START)
    }

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
            object : PeerConnectionAdapter(
                "PC:$socketId"
            ) {
                override fun onIceCandidate(iceCandidate: IceCandidate) {
                    super.onIceCandidate(iceCandidate)
                    get()!!.sendIceCandidate(iceCandidate, socketId!!)

                }

                override fun onAddStream(mediaStream: MediaStream) {
                    super.onAddStream(mediaStream)
//                val remoteVideoTrack = mediaStream.videoTracks[0]
//                runOnUiThread {
//                }
                }
            })
        peerConnection!!.addStream(mediaStream)
        peerConnectionMap!![socketId] = peerConnection
        return peerConnection
    }

    override fun onCreateRoom() {
        Log.d(TAG, "onCreateRoom")
    }

    override fun onPeerJoined(socketId: String?) {
        Log.d(TAG, "onPeerJoined"+socketId)
        val peerConnection = getOrCreatePeerConnection(socketId)
        peerConnection!!.createOffer(object : SdpAdapter("createOfferSdp:$socketId") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)

                var addViewer = Integer.parseInt(viewer)
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

    override fun onSelfJoined(userData: String ?) {
        Log.d(TAG, "onSelfJoined"+userData)
    }



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

    override fun addViewerList(message: String?) {
        Log.d(TAG, "addViewerList"+message)
        val jsonArray = JSONArray(message.toString())
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val socketId = jsonObject.getString("socketId")
            val nickName = jsonObject.getString("nickName")
            val profile = jsonObject.getString("profile")
            val userId = jsonObject.getString("userId")
            val liveStreamingViewerData= LiveStreamingViewerListData(nickName, profile,socketId,userId )

            runOnUiThread {
                liveStreamingViewerList.add(liveStreamingViewerData)
                liveStreamingViewerAdapter.notifyDataSetChanged()
                viewerListSetting()
            }

        }
    }

    override fun onLiveStreamingFinish() {
        Log.d(TAG, "onLiveStreamingFinish")
    }

    override fun onOutViewer(message: String?) {
        Log.d(TAG, "onOutViewer")
        var outViewer = Integer.parseInt(viewer)
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

        retrofit = RetrofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
        retrofitService.requestFinishLiveStreamingPost(roomIdx!!)
            .enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.isSuccessful) {

                        val jsonObject = JSONObject(response.body().toString())
//                        finish()

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

    //Peer간의 연결을 끊어주고 비디오 백그라운드에서 돌고있는 Render를 종료한다
    //localView release시켜줌
    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        Log.d("PeerHashMap", " $peerConnectionMap")
        get()!!.destroy()
        if (peerConnection == null) {
            videoCapturer!!.dispose()
        } else {
            peerConnection!!.dispose()
            videoCapturer!!.dispose()
            // 더 이상 카메라 eglRender가 돌아가지않도록 release ;
        }
        localStreamingView!!.release() // 더 이상 카메라 eglRender가 돌아가지않도록 release ;


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
    /////백버튼 눌렀을 때
    override fun onBackPressed() {
//        super.onBackPressed() ///오버라이드 하기위해선 super 받으면 안됨 .
        val builder = AlertDialog.Builder(this)
        builder.setTitle("종료")
        builder.setMessage("방송을 종료하시겠습니까?")

        builder.setPositiveButton("네") { dialog, which ->
            Toast.makeText(applicationContext,
                "방송을 종료합니다", Toast.LENGTH_SHORT).show()

            get()!!.liveStreamingFinish(roomIdx!!)
            finish()
        }
        builder.setNegativeButton("아니요") { dialog, which ->
        }
        builder.show()
    }

}