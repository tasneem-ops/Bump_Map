package com.example.mapsdemo.work

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.CoroutineWorker

import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.mapsdemo.data.local.BumpDatabase
import com.example.mapsdemo.data.model.Bump
import com.example.mapsdemo.data.model.BumpData
import com.example.mapsdemo.data.model.SpeedCamera
import com.example.mapsdemo.data.repository.LocalRepository
import com.example.mapsdemo.geofence.GeofencesUtilFunctions
import com.example.mapsdemo.utils.BumpSavedNotification
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

private const val TAG = "WorkManager"
class RefreshGeofenceWorker(appContext: Context, params: WorkerParameters):
    CoroutineWorker(appContext, params){
    companion object{
        const val WORK_NAME = "RefreshGeofenceWorker"
    }
    override suspend fun doWork(): Result {
        val appContext = applicationContext
        return try{
            Result.success()
        }
        catch (e: Throwable){
            Log.d(TAG, e.message.toString())
            Result.retry()
        }
    }
}
