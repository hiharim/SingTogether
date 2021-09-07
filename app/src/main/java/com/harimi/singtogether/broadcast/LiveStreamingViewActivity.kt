package com.harimi.singtogether.broadcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.harimi.singtogether.Data.LocalChattingData
import com.harimi.singtogether.Data.RemoteChattingData
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.R
import com.harimi.singtogether.adapter.LocalChattingAdapter
import com.harimi.singtogether.adapter.RemoteChattingAdapter
import org.json.JSONObject
import org.webrtc.*
import org.webrtc.audio.JavaAudioDeviceModule
import java.util.ArrayList
import java.util.HashMap
import com.harimi.singtogether.broadcast.SignalingClient.Companion.get
import org.json.JSONArray


class LiveStreamingViewActivity : AppCompatActivity() , SignalingClient.Callback{
    var audioConstraints: MediaConstraints? = null
    var audioSource: AudioSource? = null
    var localAudioTrack: AudioTrack? = null
    var remoteStreamingView: SurfaceViewRenderer? = null
    var roomIdx: String? = null
    private val TAG = "JOIN_ACTIVITY"
    var eglBaseContext: EglBase.Context? = null
    var peerConnectionFactory: PeerConnectionFactory? = null
    var peerConnection: PeerConnection? = null
    var mediaStream: MediaStream? = null
    var iceServers: MutableList<PeerConnection.IceServer>? = null
    var remoteVideoTrack: VideoTrack? = null
    var peerConnectionMap: HashMap<String?, PeerConnection?>? = null
    var i : Int =0
    private var messageArea : Boolean = false

    private lateinit var activity_streaming_btn_close :ImageButton
    private lateinit var activity_streaming_btn_chat :ImageButton
    private lateinit var et_chattingInputText :EditText
    private lateinit var btn_sendInputText :Button
    private val remoteChattingList: ArrayList<RemoteChattingData> = ArrayList()
    private lateinit var rv_chattingRecyclerView : RecyclerView
    private lateinit var remoteChattingAdapter: RemoteChattingAdapter
    private lateinit var activity_streaming_tv_count: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_streaming_view)

        val getintent = intent
        roomIdx = getintent.getStringExtra("roomIdx")
        Log.d(TAG," "+ roomIdx)



        activity_streaming_btn_close = findViewById<ImageButton>(R.id.activity_streaming_btn_close)
        activity_streaming_btn_chat = findViewById<ImageButton>(R.id.activity_streaming_btn_chat) ////채팅창 visible 설정
        activity_streaming_tv_count = findViewById<TextView>(R.id.activity_streaming_tv_count)
        et_chattingInputText = findViewById<EditText>(R.id.et_chattingInputText)
        btn_sendInputText = findViewById<Button>(R.id.btn_sendInputText)
        rv_chattingRecyclerView = findViewById(R.id.rv_chattingRecyclerView)
        rv_chattingRecyclerView.layoutManager = LinearLayoutManager(this)
        rv_chattingRecyclerView.addItemDecoration(
            DividerItemDecoration( this,
                DividerItemDecoration.VERTICAL)
        )
        remoteChattingAdapter = RemoteChattingAdapter(remoteChattingList,this)
        rv_chattingRecyclerView.adapter = remoteChattingAdapter


        peerConnectionMap = HashMap()
        iceServers = ArrayList()
        iceServers!!.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        eglBaseContext = EglBase.create().eglBaseContext
        // create PeerConnectionFactory
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
            .builder(this)
            .createInitializationOptions())
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
//        SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext);
//        VideoCapturer videoCapturer = createCameraCapturer(true);
//        VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
//        videoCapturer.initialize(surfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());
//        videoCapturer.startCapture(480, 640, 30);
//        VideoTrack videoTrack = peerConnectionFactory.createVideoTrack("100", videoSource);


        //오디오 트랙 채널과 소스
        audioConstraints = MediaConstraints()

        audioSource = peerConnectionFactory!!.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory!!.createAudioTrack("101", audioSource)
        localAudioTrack!!.setVolume(10.0)
        mediaStream = peerConnectionFactory?.createLocalMediaStream("mediaStream")

        //미디어 스트림에 비디오트랙 넣기
//        mediaStream.addTrack(videoTrack);
//        //미디어 스트림에 오디오 트랙에 넣기
//        mediaStream.addTrack(localAudioTrack);

        remoteStreamingView = findViewById(R.id.remoteStreamingView)
        remoteStreamingView!!.setMirror(true)
        remoteStreamingView!!.init(eglBaseContext, null)
        get()!!.init(this, roomIdx)


        ////채팅보내기
        btn_sendInputText.setOnClickListener {
            val chattingText = et_chattingInputText.text.toString()
            et_chattingInputText.setText("")
            get()!!.chattingInput(roomIdx!!, LoginActivity.user_info.loginUserNickname,chattingText,LoginActivity.user_info.loginUserProfile)
        }

        ////방송나가기
        activity_streaming_btn_close.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("종료")
            builder.setMessage("방송을 그만보시겠습니까?")

            builder.setPositiveButton("네") { dialog, which ->

                get()!!.outViewer(roomIdx!!)
                finish()
            }
            builder.setNegativeButton("아니요") { dialog, which ->
            }
            builder.show()
    }

        ///채팅 VISIBLE 설정
        activity_streaming_btn_chat.setOnClickListener {
            if(messageArea){
                messageArea = false
                rv_chattingRecyclerView.visibility  = View.VISIBLE
                et_chattingInputText.visibility = View.VISIBLE
                btn_sendInputText.visibility = View.VISIBLE

            }else{
                messageArea = true
                rv_chattingRecyclerView.visibility  = View.INVISIBLE
                et_chattingInputText.visibility = View.INVISIBLE
                btn_sendInputText.visibility = View.INVISIBLE
            }
        }

    }

    @Synchronized
    private fun getOrCreatePeerConnection(socketId: String?): PeerConnection? {
        Log.d(TAG, "getOrCreatePeerConnection")
        peerConnection = peerConnectionMap!![socketId]
        if (peerConnection != null) {
            return peerConnection
        }
        peerConnection = peerConnectionFactory!!.createPeerConnection(iceServers, object : PeerConnectionAdapter("PC :$socketId") {
            override fun onIceCandidate(iceCandidate: IceCandidate) {
                super.onIceCandidate(iceCandidate)
                get()!!.sendIceCandidate(iceCandidate, socketId!!)
            }

            override fun onAddStream(mediaStream: MediaStream) {
                super.onAddStream(mediaStream)
//                if (i!=0){
//                    remoteStreamingView!!.release()
//                }else{
//
//                }
                remoteVideoTrack = mediaStream.videoTracks[0]
                Log.d(TAG, "" + remoteVideoTrack)
                runOnUiThread { remoteVideoTrack?.addSink(remoteStreamingView) }

                i += 1
//
                Log.d(TAG, "" + i)
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
        Log.d(TAG, "onPeerJoined")
        val peerConnection = getOrCreatePeerConnection(socketId)
        peerConnection!!.createOffer(object : SdpAdapter("createOfferSdp:$socketId") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                peerConnection.setLocalDescription(SdpAdapter("setLocalSdp:$socketId"), sessionDescription)
                get()!!.sendSessionDescription(sessionDescription, socketId!!)
            }
        }, MediaConstraints())
    }

    override fun onSelfJoined() {
        Log.d(TAG, "onSelfJoined")
    }

    override fun onGetMessage(message: String?) {
        Log.d(TAG, "onGetMessage")

        val jsonArray = JSONArray(message.toString())
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val chattingText = jsonObject.getString("inputText")
            val nickName = jsonObject.getString("nickName")
            val profile = jsonObject.getString("profile")
            val remotechattingdata = RemoteChattingData(nickName,chattingText,profile)

            runOnUiThread {
                remoteChattingList.add( remotechattingdata)
                remoteChattingAdapter.notifyDataSetChanged()
            }

        }
    }

    override fun onGetViewer(message: String?) {
        Log.d(TAG, "onGetViewer")

        val jsonArray = JSONArray(message.toString())
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val getViewer = jsonObject.getString("viewer")
            runOnUiThread {
                activity_streaming_tv_count.setText(getViewer)
            }

        }
    }

    override fun onLiveStreamingFinish() {
        Log.d(TAG, "onLiveStreamingFinish")
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("종료")
            builder.setMessage("방송이 종료되었습니다")

            builder.setPositiveButton("확인") { dialog, which ->
                finish()
            }
            builder.show()
        }
    }

    override fun onOutViewer() {
        Log.d(TAG, "onOutViewer")
    }


    override fun onPeerLeave(msg: String?) {
        println("호출 확인 : $msg")
    }

    override fun onOfferReceived(data: JSONObject?) {
        Log.d(TAG, "onOfferReceived" + data.toString())
        runOnUiThread {
            val socketId = data!!.optString("from")
            val peerConnection = getOrCreatePeerConnection(socketId)
            peerConnection!!.setRemoteDescription(SdpAdapter("setRemoteSdp:$socketId"),
                SessionDescription(SessionDescription.Type.OFFER, data.optString("sdp")))
            peerConnection.createAnswer(object : SdpAdapter("localAnswerSdp") {
                override fun onCreateSuccess(sdp: SessionDescription) {
                    super.onCreateSuccess(sdp)
                    peerConnectionMap!![socketId]!!.setLocalDescription(SdpAdapter("setLocalSdp:$socketId"), sdp)
                    get()!!.sendSessionDescription(sdp, socketId)
                }
            }, MediaConstraints())
        }
    }

    override fun onAnswerReceived(data: JSONObject?) {
        Log.d(TAG, "onAnswerReceived" + data.toString())
        val socketId = data!!.optString("from")
        val peerConnection = getOrCreatePeerConnection(socketId)
        peerConnection!!.setRemoteDescription(SdpAdapter("setRemoteSdp:$socketId"),
            SessionDescription(SessionDescription.Type.ANSWER, data.optString("sdp")))
    }

    override fun onIceCandidateReceived(data: JSONObject?) {
        Log.d(TAG, "onIceCandidateReceived" + data.toString())
        val socketId = data!!.optString("from")
        val peerConnection = getOrCreatePeerConnection(socketId)
        peerConnection!!.addIceCandidate(IceCandidate(
            data.optString("id"),
            data.optInt("label"),
            data.optString("candidate")
        ))
    }

    override fun onDestroy() { //앱 죽여 버릴떄 호출됨
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        get()!!.destroy() // 소켓으로 끊어 달라고 쏴줌


        if (peerConnection == null) {
        } else {
            peerConnection!!.dispose()
        }
        remoteStreamingView!!.release() /// 더 이상 eglRender 가 돌아가지 않도록 release 해준다 .
    }

    override fun onBackPressed() {
        super.onBackPressed()
        println("뒤로가기 버튼 누름 ")
        get()!!.outViewer(roomIdx!!)
        //        PackageManager packageManager = getApplicationContext().getPackageManager();
//        Intent intent = packageManager.getLaunchIntentForPackage(getApplicationContext().getPackageName());
//        getApplicationContext().startActivity(intent);
//        Runtime.getRuntime().exit(0);
    }
}