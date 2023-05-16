package com.example.mapsdemo.geofence

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.example.mapsdemo.R
import com.example.mapsdemo.bluetooth.BluetoothService
import com.example.mapsdemo.map_screen.MapsActivity
import com.example.mapsdemo.data.local.BumpDao
import com.example.mapsdemo.data.local.BumpDatabase
import com.example.mapsdemo.data.repository.DataSource
import com.example.mapsdemo.data.repository.LocalRepository
import com.example.mapsdemo.map_screen.MapsActivity.Companion.m_bluetoothChatService
import com.example.mapsdemo.utils.sendBumpNotification
import com.example.mapsdemo.utils.sendSpeedCameraNotification
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule
import kotlin.coroutines.CoroutineContext
class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob
    private var database : BumpDatabase? = null
    private var repository : DataSource? = null

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
        val sharedPreferences = this.getSharedPreferences(getString(R.string.app_name),
        Context.MODE_PRIVATE)
        val distance = sharedPreferences.getInt(getString(R.string.shared_pref_distance_key), Context.MODE_PRIVATE)
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
                try {
                    database = BumpDatabase.getInstance(this@GeofenceTransitionsJobIntentService)
                    if (database!= null)
                    repository = LocalRepository(database!!)
                }
                catch (e: Exception){
                    Log.d("GeofenceTransitionsJobIntentService", e.message.toString())
                }
                sendNotification(triggeringGeofences, distance)
            }
        }
    }

    fun sendNotification(triggeringGeofences: List<Geofence>, distance : Int) {
        for (triggeringGeofence in triggeringGeofences){
            val latitude = triggeringGeofence.latitude
            val longitude = triggeringGeofence.longitude
            try {
                val requestId = triggeringGeofence.requestId
                CoroutineScope(coroutineContext).launch(SupervisorJob()){
                    val speedCamera = repository?.getSpeedCameraById(requestId)
                    if (speedCamera != null){
                        sendSpeedCameraNotification(this@GeofenceTransitionsJobIntentService)
                    }
                    else{
                        sendBumpNotification(
                            this@GeofenceTransitionsJobIntentService
                        , latitude, longitude
                        )
                        sendCommand("n${distance}\n")
                    }
                }
            }
            catch (e:Exception){
                Log.d("GeofenceTransitionsJobIntentService", e.message.toString())
                sendBumpNotification(
                    this@GeofenceTransitionsJobIntentService
                    , latitude, longitude
                )
                sendCommand("n${distance}\n")
            }
            }
        }
    private fun sendData(){
        sendCommand("Bump: ${GeofencingConstants.GEOFENCE_RADIUS_IN_METERS}\n")
        sendCommand("#")
    }
    private fun sendCommand(char : String) {
        val out = char.toByteArray()
        m_bluetoothChatService?.write(out)
        //Old Code.. Not used Any More
//        if (BluetoothService.embeddedSocket != null) {
//            try{
//                BluetoothService.embeddedSocket!!.outputStream.write(input.toByteArray())
//            } catch(e: IOException) {
//                e.printStackTrace()
//            }
//        }
    }
    }

