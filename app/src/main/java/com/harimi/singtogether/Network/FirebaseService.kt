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
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.harimi.singtogether.LoginActivity
import com.harimi.singtogether.MainActivity
import com.harimi.singtogether.R
import com.harimi.singtogether.broadcast.DetailReplayActivity
import com.harimi.singtogether.sing.DetailDuetFragment

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


        if (message.data["category"].toString().equals("리플레이")) {
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

        }else if(message.data["category"].toString().equals("송포스트")) {
            if (LoginActivity.user_info.loginUserEmail.equals("")){
                LoginActivity.user_info.loginUserNickname = message.data["uploadUserNickName"].toString()
                LoginActivity.user_info.loginUserProfile = message.data["uploadUserProfile"].toString()
                LoginActivity.user_info.loginUserFCMToken = message.data["uploadUserFCMToken"].toString()
                LoginActivity.user_info.loginUserEmail = message.data["uploadUserEmail"].toString()
            }

            val intent = Intent(this, MainActivity::class.java)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationID = Random.nextInt()

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(notificationManager)
            }
            intent.putExtra("Notification","PostFragment")
            val bundle = Bundle()
            bundle.putInt("idx", message.data["idx"]!!.toInt())
            bundle.putInt("mr_idx", message.data["mr_idx"]!!.toInt())
            bundle.putString("thumbnail",message.data["thumbnail"].toString())
            bundle.putString("title", message.data["title"].toString())
            bundle.putString("singer", message.data["singer"].toString())
            bundle.putString("cnt_play",  message.data["cnt_play"].toString())
            bundle.putString("cnt_reply", message.data["cnt_reply"].toString())
            bundle.putString("cnt_like", message.data["cnt_like"].toString())
            bundle.putString("email", message.data["email"].toString())
            bundle.putString("nickname",  message.data["nickname"].toString())
            bundle.putString("collabo_email", message.data["collabo_email"].toString())
            bundle.putString("collaboration_nickname", message.data["collaboration_nickname"].toString())
            bundle.putString("song_path", message.data["song_path"].toString())
            bundle.putString("profile", message.data["profile"].toString())
            bundle.putString("collaboration_profile", message.data["collaboration_profile"].toString())
            bundle.putString("date", message.data["date"].toString())
            bundle.putString("kinds", message.data["kinds"].toString())
            bundle.putString("token", message.data["token"].toString())
            bundle.putString("col_token", message.data["col_token"].toString())
            bundle.putString("isLiked",  message.data["isLiked"].toString())
            intent.putExtra("bundle",bundle)

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

        }else if(message.data["category"].toString().equals("듀엣")) {
            if (LoginActivity.user_info.loginUserEmail.equals("")){
                LoginActivity.user_info.loginUserNickname = message.data["uploadUserNickName"].toString()
                LoginActivity.user_info.loginUserProfile = message.data["uploadUserProfile"].toString()
                LoginActivity.user_info.loginUserFCMToken = message.data["uploadUserFCMToken"].toString()
                LoginActivity.user_info.loginUserEmail = message.data["uploadUserEmail"].toString()
            }

            val intent = Intent(this, MainActivity::class.java)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationID = Random.nextInt()

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(notificationManager)
            }
            intent.putExtra("Notification","DetailDuetFragment")
            val bundle = Bundle()
            bundle.putInt("duet_idx", message.data["duet_idx"]!!.toInt())
            bundle.putInt("mr_idx", message.data["mr_idx"]!!.toInt())
            bundle.putString("thumbnail",message.data["thumbnail"].toString())
            bundle.putString("title", message.data["title"].toString())
            bundle.putString("singer", message.data["singer"].toString())
            bundle.putString("cnt_play",  message.data["cnt_play"].toString())
            bundle.putString("cnt_reply", message.data["cnt_reply"].toString())
            bundle.putString("cnt_duet", message.data["cnt_duet"].toString())
            bundle.putString("email", message.data["email"].toString())
            bundle.putString("nickname",  message.data["nickname"].toString())
            bundle.putString("duet_path", message.data["duet_path"].toString())
            bundle.putString("mr_path", message.data["mr_path"].toString())
            bundle.putString("extract_path", message.data["extract_path"].toString())
            bundle.putString("profile", message.data["profile"].toString())
            bundle.putString("date", message.data["date"].toString())
            bundle.putString("kinds", message.data["kinds"].toString())
            bundle.putString("lyrics",  message.data["lyrics"].toString())
            bundle.putString("token",message.data["token"].toString())
            intent.putExtra("bundle",bundle)

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
