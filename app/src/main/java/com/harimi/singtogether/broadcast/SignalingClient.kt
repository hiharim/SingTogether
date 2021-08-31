package com.harimi.singtogether.broadcast

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import java.net.URISyntaxException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class SignalingClient private constructor() {
    private val TAG = "MAIN_ACTIVITY_SIGNALING"
    private var mRoomName: String? = null
    private var socket: Socket? = null

    //        private String room = "OldPlace";
    private var callback: Callback? = null
    private val trustAll = arrayOf<TrustManager>(
            object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate?> {
                    return arrayOfNulls(0)
                }
            }
    )

    fun init(callback: Callback, roomName: String?) {
        Log.d(TAG, "init")
        mRoomName = roomName
        Log.d(TAG, "mRoomName: $mRoomName")
        this.callback = callback
        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAll, null)
            IO.setDefaultHostnameVerifier { hostname: String?, session: SSLSession? -> true }
            IO.setDefaultSSLContext(sslContext)
            socket = IO.socket("https://3.35.236.251:8080")
            socket!!.connect()
            socket!!.emit("create or join", roomName)
            Log.d(TAG, "create or join")
            socket!!.on("created", Emitter.Listener { args: Array<Any?>? ->
                Log.d(TAG, "created")
                Log.e("chao", "room created:" + socket!!.id())
                callback.onCreateRoom()
            })
            socket!!.on("full", Emitter.Listener { args: Array<Any?>? ->
                Log.d(TAG, "full")
                Log.e("chao", "room full")
            })
            socket!!.on("join", Emitter.Listener { args: Array<Any> ->
                Log.d(TAG, "join")
                Log.e("chao", "peer joined " + Arrays.toString(args))
                callback.onPeerJoined(args[1].toString())
            })
            socket!!.on("joined", Emitter.Listener { args: Array<Any?>? ->
                Log.d(TAG, "joined")
                Log.e("chao", "self joined:" + socket!!.id())
                callback.onSelfJoined()
            })
            socket!!.on("log", Emitter.Listener { args: Array<Any?>? ->
                Log.d(TAG, TAG + "log")
                Log.e("chao", "log call " + Arrays.toString(args))
            })
            socket!!.on("bye", Emitter.Listener { args: Array<Any> ->
                Log.d(TAG, "bye")
                Log.e("chao", "bye " + args[0])
                callback.onPeerLeave(args[0] as String)
            })
            socket!!.on("message", Emitter.Listener { args: Array<Any?> ->
                Log.d(TAG, "message")
                Log.e("chao", "message " + Arrays.toString(args))
                val arg = args[0]
                if (arg is String) {
                } else if (arg is JSONObject) {
                    val data = arg
                    val type = data.optString("type")
                    Log.d(TAG, "message$type")
                    if ("offer" == type) {
                        callback.onOfferReceived(data)
                    } else if ("answer" == type) {
                        callback.onAnswerReceived(data)
                    } else if ("candidate" == type) {
                        callback.onIceCandidateReceived(data)
                    }
                }
            })
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: KeyManagementException) {
            e.printStackTrace()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun destroy() {
        Log.d(TAG, "destroy")
        //            socket.emit("bye", socket.id() ,mRoomName);
        val jo = JSONObject()
        try {
            jo.put("from", socket!!.id())
            jo.put("room", mRoomName)
            socket!!.emit("bye", jo)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        socket!!.disconnect()
        socket!!.close()
        socket!!.off()
        instance = null
    }

    fun sendIceCandidate(iceCandidate: IceCandidate, to: String) {
        Log.d(TAG, "sendIceCandidate$to")
        //            Log.d(TAG ,"sendSessionDescription" +iceCandidate.toString());
        val jo = JSONObject()
        try {
            jo.put("type", "candidate")
            jo.put("label", iceCandidate.sdpMLineIndex)
            jo.put("id", iceCandidate.sdpMid)
            jo.put("candidate", iceCandidate.sdp)
            jo.put("from", socket!!.id())
            jo.put("room", mRoomName)
            jo.put("to", to)
            socket!!.emit("message", jo)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun sendSessionDescription(sdp: SessionDescription, to: String) {
        Log.d(TAG, "sendSessionDescription$to")
        //            Log.d(TAG ,"sendSessionDescription" +sdp.toString());
        val jo = JSONObject()
        try {
            jo.put("type", sdp.type.canonicalForm())
            jo.put("sdp", sdp.description)
            jo.put("from", socket!!.id())
            jo.put("to", to)
            socket!!.emit("message", jo)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    interface Callback {
        fun onCreateRoom()
        fun onPeerJoined(socketId: String?)
        fun onSelfJoined()
        fun onPeerLeave(msg: String?)
        fun onOfferReceived(data: JSONObject?)
        fun onAnswerReceived(data: JSONObject?)
        fun onIceCandidateReceived(data: JSONObject?)
    }

    companion object {
        private var instance: SignalingClient? = null
        @JvmStatic
        fun get(): SignalingClient? {
            if (instance == null) {
                synchronized(SignalingClient::class.java) {
                    if (instance == null) {
                        instance = SignalingClient()
                    }
                }
            }
            return instance
        }
    }
}