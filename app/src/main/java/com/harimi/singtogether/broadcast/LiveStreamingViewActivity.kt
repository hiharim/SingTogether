package com.harimi.singtogether.broadcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.harimi.singtogether.R
import org.json.JSONObject
import org.webrtc.*
import org.webrtc.audio.JavaAudioDeviceModule
import java.util.ArrayList
import java.util.HashMap
import com.harimi.singtogether.broadcast.SignalingClient.Companion.get


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_streaming_view)

        val getintent = intent
        roomIdx = getintent.getStringExtra("roomIdx")
        Log.d(TAG," "+ roomIdx)
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
                Log.d("onAddStreamRemote", "" + mediaStream.videoTracks[0].toString())
                Log.d("onAddStreamRemote", "" + remoteVideoTrack)
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

    override fun onSelfJoined() {
        Log.d(TAG, "onSelfJoined")
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
        //        PackageManager packageManager = getApplicationContext().getPackageManager();
//        Intent intent = packageManager.getLaunchIntentForPackage(getApplicationContext().getPackageName());
//        getApplicationContext().startActivity(intent);
//        Runtime.getRuntime().exit(0);
    }
}