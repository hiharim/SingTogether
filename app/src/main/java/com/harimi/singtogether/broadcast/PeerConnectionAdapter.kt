package com.harimi.singtogether.broadcast

import android.util.Log
import org.webrtc.*
import org.webrtc.PeerConnection.*

open class PeerConnectionAdapter(tag: String) : PeerConnection.Observer {
    private val TAG = "MAIN_ACTIVITY_PeerConnectionAdapter"
    private val tag: String
    private fun log(s: String) {
        Log.d(tag, s)
    }

    override fun onSignalingChange(signalingState: SignalingState) {
        Log.d(TAG, "onSignalingChange")
        log("onSignalingChange $signalingState")
    }

    override fun onIceConnectionChange(iceConnectionState: IceConnectionState) {
        Log.d(TAG, "onIceConnectionChange")
        log("onIceConnectionChange $iceConnectionState")
    }

    override fun onIceConnectionReceivingChange(b: Boolean) {
        Log.d(TAG, "onIceConnectionReceivingChange")
        log("onIceConnectionReceivingChange $b")
    }

    override fun onIceGatheringChange(iceGatheringState: IceGatheringState) {
        Log.d(TAG, "onIceGatheringChange")
        log("onIceGatheringChange $iceGatheringState")
    }

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        Log.d(TAG, "onIceCandidate")
        log("onIceCandidate $iceCandidate")
    }

    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {
        Log.d(TAG, "onIceCandidatesRemoved")
        log("onIceCandidatesRemoved $iceCandidates")
    }

    override fun onAddStream(mediaStream: MediaStream) {
        Log.d(TAG, "onAddStream")
        log("onAddStream $mediaStream")
    }

    override fun onRemoveStream(mediaStream: MediaStream) {
        Log.d(TAG, "onRemoveStream")
        log("onRemoveStream $mediaStream")
    }

    override fun onDataChannel(dataChannel: DataChannel) {
        Log.d(TAG, "onDataChannel")
        log("onDataChannel $dataChannel")
    }

    override fun onRenegotiationNeeded() {
        Log.d(TAG, "onRenegotiationNeeded")
        log("onRenegotiationNeeded ")
    }

    override fun onAddTrack(rtpReceiver: RtpReceiver, mediaStreams: Array<MediaStream>) {
        Log.d(TAG, "onAddTrack")
        log("onAddTrack $mediaStreams")
    }

    init {
        this.tag = "chao $tag"
    }
}