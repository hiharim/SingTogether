package com.harimi.singtogether.Network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.harimi.singtogether.MainActivity
import com.harimi.singtogether.R

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private  val TAG = "MyFirebaseMsgService"

    private lateinit var title:String;
    private lateinit var message:String;


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)


        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob()
            } else {
                // Handle message within 10 seconds
                handleNow()
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
//        title = remoteMessage.getData().get("Title").toString()
//        message = remoteMessage.getData().get("Message").toString()
//        val builder = NotificationCompat.Builder(applicationContext)
//            .setSmallIcon(android.R.drawable.stat_sys_download)
//            .setContentTitle(title)
//            .setContentText(message)
//        val manager =
//            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        manager.notify(0, builder.build())

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
        sendRegistrationToServer(token)
        } // [END on_new_token]
    private fun scheduleJob() {
        // [START dispatch_job]
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        WorkManager.getInstance(this).beginWith(work).enqueue()
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.goodbye)
            .setContentTitle(getString(R.string.fcm_message))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"
    }
}


