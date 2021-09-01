package com.harimi.singtogether.broadcast

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.harimi.singtogether.HomeFragment
import com.harimi.singtogether.MainActivity
import com.harimi.singtogether.Network.RetrofitClient
import com.harimi.singtogether.Network.RetrofitService
import com.harimi.singtogether.R
import com.harimi.singtogether.broadcast.SignalingClient.Companion.get
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
    private lateinit var activity_streaming_tv_count :TextView
    private lateinit var activity_streaming_btn_close :ImageView
    private lateinit var activity_streaming_btn_switch_cam :ImageButton

    private var viewer : String ? ="0"

    var localStreamingView: SurfaceViewRenderer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_streaming)

        val getIntent = intent
        roomIdx = getIntent.getStringExtra("roomIdx")
        Log.d(TAG, " $roomIdx")
        activity_streaming_tv_count = findViewById<TextView>(R.id.activity_streaming_tv_count) //방송 시청자
        activity_streaming_btn_close = findViewById<ImageView>(R.id.activity_streaming_btn_close)// 나가기 버튼
        activity_streaming_btn_switch_cam =findViewById(R.id.activity_streaming_btn_switch_cam)// 카메라 전환 버튼
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
        val videoSource = peerConnectionFactory!!.createVideoSource(videoCapturer!!.isScreencast)
        videoCapturer!!.initialize(
            surfaceTextureHelper,
            applicationContext,
            videoSource?.capturerObserver
        )
        videoCapturer!!.startCapture(480, 640, 30)
        val videoTrack = peerConnectionFactory!!.createVideoTrack("100", videoSource)

        localStreamingView = findViewById(R.id.localStreamingView)
        localStreamingView!!.setMirror(true)
        localStreamingView!!.init(eglBaseContext, null)


        //오디오 트랙 채널과 소스
        audioConstraints = MediaConstraints()
        //        audioConstraints.mandatory.add(
//                new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
//        audioConstraints.mandatory.add(
//                new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
//        audioConstraints.mandatory.add(
//                new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
//        audioConstraints.mandatory.add(
//                new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true"));
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



        activity_streaming_btn_switch_cam.setOnClickListener {

            videoCapturer!!.dispose()
            localStreamingView!!.release()
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

            peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(defaultVideoEncoderFactory)
                .setVideoDecoderFactory(defaultVideoDecoderFactory)
                .createPeerConnectionFactory()


            //비디오 트랙 채널과 소스
            val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBaseContext)
            videoCapturer = createCameraCapturer(false) ///카메라를 정면선택할지 후면선택할지 선택
            val videoSource = peerConnectionFactory!!.createVideoSource(videoCapturer!!.isScreencast)
            videoCapturer!!.initialize(
                surfaceTextureHelper,
                applicationContext,
                videoSource?.capturerObserver
            )
            videoCapturer!!.startCapture(480, 640, 30)
            val videoTrack = peerConnectionFactory!!.createVideoTrack("100", videoSource)

            localStreamingView!!.setMirror(true)
            localStreamingView!!.init(eglBaseContext, null)


            videoTrack!!.addSink(localStreamingView)

        }
        ////나가기 버튼 눌렀을 때
        activity_streaming_btn_close.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("종료")
            builder.setMessage("방송을 종료하시겠습니까?")

            builder.setPositiveButton("네") { dialog, which ->
                Toast.makeText(applicationContext,
                    "방송을 종료합니다", Toast.LENGTH_SHORT).show()
                finish()
            }
            builder.setNegativeButton("아니요") { dialog, which ->
            }
            builder.show()
        }

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
        Log.d(TAG, "onPeerJoined")
        val peerConnection = getOrCreatePeerConnection(socketId)
        peerConnection!!.createOffer(object : SdpAdapter("createOfferSdp:$socketId") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)

                var addViewer = Integer.parseInt(viewer)
                addViewer++
                viewer = addViewer.toString()
                activity_streaming_tv_count.text = viewer
                Log.d(TAG, "onAddStream" + viewer)

                peerConnection.setLocalDescription(
                    SdpAdapter("setLocalSdp:$socketId"),
                    sessionDescription
                )
                get()!!.sendSessionDescription(sessionDescription, socketId!!)
            }
        }, MediaConstraints())
    }

    override fun onSelfJoined() {
        Log.d(TAG, "onSelfJoined")
    }

    override fun onPeerLeave(msg: String?) {
        Log.d(TAG, "onPeerLeave")
        Log.d(TAG, "msg")
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


    ////화면을 나가기 전에 db에 있는 파일을 지워준다 .
    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
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
//                            val roomIdx = jsonObject.getString("roomIdx")

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

        // First, try to find front facing camera
        for (deviceName in deviceNames) {
            if (if (isFront) enumerator.isFrontFacing(deviceName) else enumerator.isBackFacing(
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
}