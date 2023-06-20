package com.example.mapsdemo.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mapsdemo.BuildConfig
import com.example.mapsdemo.R
import com.example.mapsdemo.broadcastReceiver.DownVoteReceiver
import com.example.mapsdemo.broadcastReceiver.UpVoteReceiver
import com.example.mapsdemo.data.model.BumpData

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"
private const val NOTIFICATION_CHANNEL_ID_1 = BuildConfig.APPLICATION_ID +".channel_1"
private const val NOTIFICATION_CHANNEL_ID_2 = BuildConfig.APPLICATION_ID +".channel_2"

fun sendBumpNotification(context: Context, latitude : Double, longitude: Double) {
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationSound = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.bump}")
    val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .build()
    // We need to create a NotificationChannel associated with our CHANNEL_ID before sending a notification.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null
    ) {
        val name = "Bumps"
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.enableVibration(true)
        channel.setSound(notificationSound, attributes)

        notificationManager.createNotificationChannel(channel)
    }
    notificationManager.cancelAll()


//    val intent = MapsActivity.newIntent(context.applicationContext)
//    //create a pending intent that opens MapsActivity when the user clicks on the notification
//    val stackBuilder = TaskStackBuilder.create(context)
//        .addParentStack(MapsActivity::class.java)
//        .addNextIntent(intent)
//    val notificationPendingIntent = stackBuilder
//        .getPendingIntent(getUniqueId(), PendingIntent.FLAG_UPDATE_CURRENT)
    val vibrate = LongArray(4)
    vibrate[0] = 0L
    vibrate[1] = 100L
    vibrate[2] = 200L
    vibrate[3] = 300L

    val upVoteIntent = Intent(context, UpVoteReceiver::class.java)
    upVoteIntent.putExtra("latitude", latitude)
    upVoteIntent.putExtra("longitude", longitude)
    val pendingUpIntent = PendingIntent.getBroadcast(context, 2, upVoteIntent, PendingIntent.FLAG_CANCEL_CURRENT)

    val downVoteIntent = Intent(context, DownVoteReceiver::class.java)
    downVoteIntent.putExtra("latitude", latitude)
    downVoteIntent.putExtra("longitude", longitude)
    val pendingDownIntent = PendingIntent.getBroadcast(context, 3, downVoteIntent, PendingIntent.FLAG_CANCEL_CURRENT)

//    build the notification object with the data to be shown
    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Bump!!")
        .setPriority(Notification.PRIORITY_MAX)
        .setContentText("There is a bump near you. Be careful !")
        .setAutoCancel(true)
        .setSound(notificationSound)
        .setVibrate(vibrate)
        .addAction( R.mipmap.ic_launcher,"Yes It's a Bump", pendingUpIntent)
        .addAction(R.mipmap.ic_launcher, "No there's No Bump", pendingDownIntent)
        .build()

    notificationManager.notify(getUniqueId(), notification)
}

fun BumpSavedNotification(context: Context) {
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationSound = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.bumpdetected}")
    val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .build()
    val vibrate = LongArray(4)
    vibrate[0] = 0L
    vibrate[1] = 100L
    vibrate[2] = 200L
    vibrate[3] = 300L
    // We need to create a NotificationChannel associated with our CHANNEL_ID before sending a notification.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID_2) == null
    ) {
        val name = "Bump Detected"
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID_2,
            name,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.enableVibration(true)
        channel.setSound(notificationSound, attributes)
        notificationManager.createNotificationChannel(channel)
    }

//    build the notification object with the data to be shown
    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_2)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Bump Saved!!")
        .setPriority(Notification.PRIORITY_MAX)
        .setAutoCancel(true)
        .setSound(notificationSound)
        .setVibrate(vibrate)
        .build()

    notificationManager.notify(getUniqueId(), notification)
}

fun sendSpeedCameraNotification(context: Context){
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationSound = Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.speed_camera}")
    val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .build()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID_1) == null
    ) {
        val name = "Radar"
        val channel1 = NotificationChannel(
            NOTIFICATION_CHANNEL_ID_1,
            name,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel1.enableVibration(true)
        channel1.setSound(notificationSound, attributes)

        notificationManager.createNotificationChannel(channel1)
    }
    val vibrate = LongArray(4)
    vibrate[0] = 0L
    vibrate[1] = 100L
    vibrate[2] = 200L
    vibrate[3] = 300L
    //    build the notification object with the data to be shown
    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_1)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("Speed Camera!!")
        .setPriority(Notification.PRIORITY_MAX)
        .setContentText("There is a speed camera near you. Be careful!")
        .setAutoCancel(true)
        .setSound(notificationSound)
        .setVibrate(vibrate)
        .build()

    notificationManager.notify(getUniqueId(), notification)

}

private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())