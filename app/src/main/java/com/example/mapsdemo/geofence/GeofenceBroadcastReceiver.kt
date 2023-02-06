package com.example.mapsdemo.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Geofence", "BroadCast receiver received a Geofence !!!")
        GeofenceTransitionsJobIntentService.enqueueWork(context!!, intent!!)
    }
}