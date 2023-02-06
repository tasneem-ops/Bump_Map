package com.example.mapsdemo.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.example.mapsdemo.BuildConfig
import com.example.mapsdemo.map_screen.MapsActivity
import com.example.mapsdemo.R
import com.example.mapsdemo.data.model.Bump
import com.google.android.gms.location.Priority

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

fun sendRealNotification(context: Context) {
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // We need to create a NotificationChannel associated with our CHANNEL_ID before sending a notification.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null
    ) {
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

//    val intent = MapsActivity.newIntent(context.applicationContext)
//    //create a pending intent that opens MapsActivity when the user clicks on the notification
//    val stackBuilder = TaskStackBuilder.create(context)
//        .addParentStack(MapsActivity::class.java)
//        .addNextIntent(intent)
//    val notificationPendingIntent = stackBuilder
//        .getPendingIntent(getUniqueId(), PendingIntent.FLAG_UPDATE_CURRENT)

//    build the notification object with the data to be shown
    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Bump!!")
        .setPriority(Notification.PRIORITY_MAX)
        .setContentText("There is a bump near you. Be careful !")
        .setAutoCancel(true)
        .build()

    notificationManager.notify(getUniqueId(), notification)
}

private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())