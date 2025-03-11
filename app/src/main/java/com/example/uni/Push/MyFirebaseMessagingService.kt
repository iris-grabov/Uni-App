package com.example.uni.Push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.uni.ChatActivity
import com.example.uni.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains a notification payload
//        remoteMessage.notification?.let {
//            // Show notification
//            sendNotification(it.body)
//        }
//
//        // Check if message contains data payload
//        remoteMessage.data.isNotEmpty().let {
//            // Handle the message data if needed
//        }

        val notification = remoteMessage.notification
        val data = remoteMessage.data
        val senderId = data["senderId"] // Get the conversationId from the payload
        val message = data["message"] ?: "New message"
        val senderName = data["senderName"] ?: "Someone"

        // Show the notification
        showNotification(senderName, message, senderId)
    }

    // This method will display the notification
    private fun showNotification(title: String, message: String, senderId: String?) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannelId = "chat_channel"
        val notificationChannel = NotificationChannel(
            notificationChannelId,
            "Chat Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(notificationChannel)

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("chatUserId", senderId) // Pass the chatUserId
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Attach the intent to open ChatActivity

        notificationManager.notify(0, notificationBuilder.build())
    }
}
