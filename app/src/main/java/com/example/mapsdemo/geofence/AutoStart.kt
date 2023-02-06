package com.example.mapsdemo.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AutoStart : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        addAllGeofences()
    }

    private fun addAllGeofences() {
        TODO("Not yet implemented")
    }
}