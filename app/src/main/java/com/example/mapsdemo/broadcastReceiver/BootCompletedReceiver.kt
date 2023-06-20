package com.example.mapsdemo.broadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mapsdemo.utils.BumpSavedNotification
import com.example.mapsdemo.work.RefreshGeofenceWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class BootCompletedReceiver: BroadcastReceiver() {
    val applicationScope = CoroutineScope(Dispatchers.Default)
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_REBOOT){
            delayedInit(context)
        }
    }

    private fun delayedInit(context: Context?) = applicationScope.launch {
        setupRecurringWork(context)
    }

    private fun setupRecurringWork(context: Context?) {
        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshGeofenceWorker>(12, TimeUnit.DAYS).build()
        context?.let {
            WorkManager
                .getInstance(it)
                .enqueueUniquePeriodicWork(
                    RefreshGeofenceWorker.WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    repeatingRequest)
        }
    }
}