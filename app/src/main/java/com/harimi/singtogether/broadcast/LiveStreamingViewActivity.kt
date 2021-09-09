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
import com.harimi.singtogether.Data.RemoteChattingData
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.PostFragment
import com.harimi.singtogether.R
import com.harimi.singtogether.adapter.RemoteChattingAdapter
import org.json.JSONObject
import org.webrtc.*
import org.webrtc.audio.JavaAudioDeviceModule
import java.util.ArrayList
import java.util.HashMap
import com.harimi.singtogether.broadcast.SignalingClient.Companion.get
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class LiveStreamingViewActivity : AppCompatActivity() , SignalingClient.Callback{
    private lateinit var retrofit : Retrofit
    private lateinit var retrofitService: RetrofitService

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

        initView()

    }
    fun initView(){


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

                get()!!.outViewer(roomIdx!!,LoginActivity.user_info.loginUserEmail)
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

                remoteVideoTrack = mediaStream.videoTracks[0]
                Log.d(TAG, "" + remoteVideoTrack)
                runOnUiThread { remoteVideoTrack?.addSink(remoteStreamingView) }
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

    override fun onSelfJoined(socketID : String?) {
        Log.d(TAG, "onSelfJoined "+socketID)
        get()!!.addViewerList(roomIdx!!,LoginActivity.user_info.loginUserNickname,LoginActivity.user_info.loginUserProfile,socketID!!,LoginActivity.user_info.loginUserEmail)
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
                rv_chattingRecyclerView.scrollToPosition(remoteChattingList.size - 1)
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

    override fun addViewerList(message: String?) {

    }

    override fun onLiveStreamingFinish() {

            Log.d(TAG, "onLiveStreamingFinish")
            runOnUiThread {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("종료")
                builder.setMessage("방송이 종료되었습니다")

                builder.setPositiveButton("확인") { dialog, which ->
//                    val bundle = Bundle()
//                    val liveFragment = LiveFragment()
//                    val transaction = supportFragmentManager.beginTransaction()
//                    transaction.add(R.id.activity_main_frame, liveFragment)
//                    transaction.commit()
                    finish()

                }
                builder.show()
            }

    }

    //시청자 나갔을 때
    override fun onOutViewer(message: String?) {
        Log.d(TAG, "onOutViewer")
        var jsonArray = JSONArray(message.toString())
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val userId = jsonObject.getString("userId")
                if (!userId.equals(LoginActivity.user_info.loginUserEmail)){
                    var getViewer = activity_streaming_tv_count.text.toString()
                    var getViewerInt = Integer.parseInt(getViewer)
                    getViewerInt--
                    runOnUiThread {
                        activity_streaming_tv_count.setText(getViewerInt.toString())
                    }
                }
            }
    }

    override fun onViewerOutOfHere(message: String?) {
        Log.d(TAG, "onOutViewer")
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("강퇴")
            builder.setMessage("강퇴당하였습니다")

            builder.setPositiveButton("확인") { dialog, which ->
                Toast.makeText(applicationContext,
                    "퇴장하였습니다.", Toast.LENGTH_SHORT).show()
                get()!!.outViewer(roomIdx!!,LoginActivity.user_info.loginUserEmail)
                finish()
            }

            builder.show()
        }
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

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
        retrofit = RetrofitClient.getInstance()
        retrofitService = retrofit.create(RetrofitService::class.java)
        retrofitService.requestOutViewer(roomIdx!!)
            .enqueue(object : Callback<String> {
                override fun onResponse(
                    call: Call<String>,
                    response: Response<String>
                ) {
                    if (response.isSuccessful) {

                        val jsonObject = JSONObject(response.body().toString())


                    } else {
//                        Log.e("onResponse", "실패 : " + response.errorBody())
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

    override fun onDestroy() { //앱 죽여 버릴때 호출됨
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        get()!!.destroy()
        if (peerConnection == null) {
        } else {
            peerConnection!!.dispose()
            // 더 이상 카메라 eglRender가 돌아가지않도록 release ;
        }
        remoteStreamingView!!.release() // 더 이상 카메라 eglRender가 돌아가지않도록 release ;

    }

    override fun onBackPressed() {

        println("뒤로가기 버튼 누름 ")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("나가기")
        builder.setMessage("방송을 나가시겠습니까?")

        builder.setPositiveButton("네") { dialog, which ->
            Toast.makeText(applicationContext,
                "퇴장하였습니다.", Toast.LENGTH_SHORT).show()
            get()!!.outViewer(roomIdx!!,LoginActivity.user_info.loginUserEmail)
            finish()
        }
        builder.setNegativeButton("아니요") { dialog, which ->
        }
        builder.show()

    }
}