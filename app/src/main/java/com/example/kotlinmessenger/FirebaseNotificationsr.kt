package com.example.kotlinmessenger

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import com.example.kotlinmessenger.Constants.AppConstants
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*
import kotlin.collections.HashMap
import com.example.kotlinmessenger.UserModel
import com.google.firebase.storage.FirebaseStorage


class FirebaseNotificationsr:FirebaseMessagingService() {
    private val apputil=AppUtil()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("token:", "IL TOKEN GENERATO è $token")
        updateToken(token)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("funzione:","chiamata onMessageReceived")
            val map: Map<String, String> = remoteMessage.data

            val title = map["title"]
            val message = map["message"]
            val hisId = map["hisId"]
            val hisImage = map["hisImage"]
            val chatId = map["chatId"]
            Log.d("VALORI:", "sono:${hisId}")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                createOreonotification(title!!, message!!, hisId!!, chatId!!)
            else createnormalnotification(title!!, message!!, hisId!!, chatId!!)
        }
    }

    fun updateToken(token:String) {
        val databaseReference = FirebaseDatabase.getInstance("https://kotlin-messenger-288bc-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(apputil.getUID()!!)
        val map:MutableMap<String, Any> = HashMap()
        map["token"]=token
        databaseReference.updateChildren(map)
    }

    fun createnormalnotification(title:String, message:String, hisId:String, chatId:String) {
        val uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder=NotificationCompat.Builder(this, AppConstants.CHANNEL_ID)
        builder.setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
            .setSound(uri)
        val intent=Intent(this,MessageActivity::class.java)
        intent.putExtra("hisId", hisId)
        intent.putExtra("chatId", chatId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent=PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        builder.setContentIntent(pendingIntent)
        val manager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(Random().nextInt(85-65), builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createOreonotification(title:String, message:String, hisId:String, chatId:String) {
        val channel = NotificationChannel(
            AppConstants.CHANNEL_ID,
            "Message",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.setShowBadge(true)
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val intent = Intent(this, MessageActivity::class.java)
        Log.d("VALORI:", "sono:${hisId}")
        intent.putExtra("hisId", hisId)
        intent.putExtra("chatId", chatId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(this, AppConstants.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(100, notification)
    }

}