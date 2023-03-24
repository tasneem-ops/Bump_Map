package com.example.mapsdemo.geofence

import com.google.android.gms.location.Geofence.NEVER_EXPIRE


internal object GeofencingConstants {

    val GEOFENCE_EXPIRATION: Long = NEVER_EXPIRE
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val CAMERA_GEOFENCE_RADIUS_IN_METERS = 200f
}
