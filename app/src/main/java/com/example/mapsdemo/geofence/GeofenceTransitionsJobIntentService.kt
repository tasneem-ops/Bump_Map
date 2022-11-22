package com.example.mapsdemo.geofence

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.example.mapsdemo.data.local.BumpDao
import com.example.mapsdemo.data.repository.LocalRepository
import com.example.mapsdemo.utils.sendRealNotification
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    @SuppressLint("LongLogTag")
    override fun onHandleWork(intent: Intent) {
        if (intent != null){
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent != null) {
                if (geofencingEvent.hasError()) {
                    val errorMessage = GeofenceStatusCodes
                        .getStatusCodeString(geofencingEvent.errorCode)
                    Log.e("GeofenceTransitionsJobIntentService", errorMessage)
                    return
                }
            }

            // Get the transition type.
            val geofenceTransition = geofencingEvent?.geofenceTransition

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                Log.d("Geofence", "Geofence entered!!")
                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                val triggeringGeofences : List<Geofence> = geofencingEvent.triggeringGeofences as List<Geofence>

                sendNotification(triggeringGeofences)
            }
        }

    }

    fun sendNotification(triggeringGeofences: List<Geofence>) {
        for (triggeringGeofence in triggeringGeofences){
            val requestId = triggeringGeofence.requestId
                    //send a notification to the user with the reminder details
                    sendRealNotification(
                        this@GeofenceTransitionsJobIntentService
                    )

            }
        }

    }

