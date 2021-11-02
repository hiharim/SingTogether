package com.harimi.singtogether.Network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.R
import com.harimi.singtogether.broadcast.DetailReplayActivity

import kotlin.random.Random
class FirebaseService : FirebaseMessagingService() {
    private val TAG = "FirebaseService_"
    private val CHANNEL_ID = "FirebaseService_"

    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
//        token = newToken
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

//        Log.d(TAG , message.data["uploadUserEmail"].toString())
//        Log.d(TAG , message.data["uploadUserProfile"].toString())
//        Log.d(TAG , message.data["uploadUserNickName"].toString())


        if (message.data["category"].toString().equals("리플레이")){
            if (LoginActivity.user_info.loginUserEmail.equals("")){
                LoginActivity.user_info.loginUserNickname = message.data["uploadUserNickName"].toString()
                LoginActivity.user_info.loginUserProfile = message.data["uploadUserProfile"].toString()
                LoginActivity.user_info.loginUserFCMToken = message.data["uploadUserFCMToken"].toString()
                LoginActivity.user_info.loginUserEmail = message.data["uploadUserEmail"].toString()
            }


            val intent = Intent(this, DetailReplayActivity::class.java)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationID = Random.nextInt()

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(notificationManager)
            }
            intent.putExtra("idx", message.data["replayIdx"].toString())
            intent.putExtra("uploadUserEmail", message.data["uploadUserEmail"].toString())
            intent.putExtra("uploadUserNickName", message.data["uploadUserNickName"].toString())
            intent.putExtra("uploadUserProfile", message.data["uploadUserProfile"].toString())
            intent.putExtra("thumbnail", message.data["thumbnail"].toString())
            intent.putExtra("uploadDate", message.data["uploadDate"].toString())
            intent.putExtra("replayTitle", message.data["replayTitle"].toString())
            intent.putExtra("replayLikeNumber", message.data["replayLikeNumber"].toString())
            intent.putExtra("replayHits", message.data["replayHits"].toString())
            intent.putExtra("replayReviewNumber", message.data["replayReviewNumber"].toString())
            intent.putExtra("replayPostLikeIdx", message.data["replayPostLikeIdx"].toString())
            intent.putExtra("liked", message.data["liked"].toString())
            intent.putExtra("replayVideo", message.data["replayVideo"].toString())
            intent.putExtra("uploadUserFCMToken", message.data["uploadUserFCMToken"].toString())

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(message.data["title"])
                .setContentText(message.data["message"])
                .setSmallIcon(R.drawable.singtogether_logo_purple)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .build()

            notificationManager.notify(notificationID, notification)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channelName"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_HIGH).apply {
            description = "My channel description"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }

}
