package com.harimi.singtogether

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private  val TAG = "MyFirebaseMsgService"

    private lateinit var title:String;
    private lateinit var message:String;


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        title = remoteMessage.getData().get("Title").toString()
        message = remoteMessage.getData().get("Message").toString()
        val builder =NotificationCompat.Builder(applicationContext)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title)
            .setContentText(message)
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(0, builder.build())
//        val title = remoteMessage.notification!!.title
//        val body = remoteMessage.notification!!.body
//        val extradata = remoteMessage.data
//
//                val Receiver = extradata["Receiver"]
//                //        String Title = extradata.get("Title");
//                val User_token = extradata["User_token"]
//                val Writer_profile = extradata["Writer_profile"]
//                val Chatting_index = extradata["Chatting_index"]
//                val Host = extradata["Host"]
//                val User_name = extradata["User_name"]
//
//
//            val intent: Intent
//            intent = Intent(this, Applied_Activity::class.java)

//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
//            val channel_id = "Obbligato"
//            val builder = NotificationCompat.Builder(applicationContext, channel_id)
////                .setSmallIcon(R.drawable.music)
//                .setContentTitle(title)
//                .setContentText(body)
//                .setAutoCancel(true)
//                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
//                .setOnlyAlertOnce(true)
//                .setContentIntent(pendingIntent)
//            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//            val id = System.currentTimeMillis().toInt()
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val channel = NotificationChannel(channel_id, "DEMO", NotificationManager.IMPORTANCE_HIGH)
//                notificationManager.createNotificationChannel(channel)
//            }
//            notificationManager.notify(id, builder.build())
        }
    //
        override fun onNewToken(token: String) {
            Log.d(TAG, "Refreshed token: $token")
        } // [END on_new_token]

    }


