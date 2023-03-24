package com.example.mapsdemo.geofence

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofencesUtilFunctions(applicationContext: Context, activity: Activity) {
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(applicationContext, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private  var geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(activity)
    @SuppressLint("MissingPermission")
    fun addGeofence(lat :Double, lng: Double, radius : Double, id : String) {
        val geofence = Geofence.Builder()
            .setRequestId(id)
            .setCircularRegion(lat,
                lng,
                GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
            )
            .setExpirationDuration(GeofencingConstants.GEOFENCE_EXPIRATION)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.d(TAG, "Gefence added")
            }
            addOnFailureListener {
                Log.d(TAG, "Gefence Not added")
            }
        }
    }
    fun removeGeofences() {

        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.d(TAG, "goefence removed")
            }
            addOnFailureListener {
                Log.d(TAG, "geofence not removed")
            }
        }
    }
    private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())
    private val TAG = "MapsActivity"

}