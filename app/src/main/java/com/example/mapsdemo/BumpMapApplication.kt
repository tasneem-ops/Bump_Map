package com.example.mapsdemo

import android.app.Application
import androidx.work.*
import com.example.mapsdemo.data.model.Bump
import com.example.mapsdemo.data.model.BumpData
import com.example.mapsdemo.work.RefreshGeofenceWorker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
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
        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshGeofenceWorker>(1, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .build()

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