package com.example.mapsdemo

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mapsdemo.work.RefreshGeofenceWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class BumpMapApplication: Application() {
    val applicationScope = CoroutineScope(Dispatchers.Default)

    private fun delayedInit() = applicationScope.launch {
        setupRecurringWork()
    }

    private fun setupRecurringWork() {
        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshGeofenceWorker>(15, TimeUnit.MINUTES).build()

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniquePeriodicWork(RefreshGeofenceWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                repeatingRequest)
    }

    override fun onCreate() {
        super.onCreate()
        delayedInit()
    }

}