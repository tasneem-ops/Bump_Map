package com.example.mapsdemo

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Handler
import android.widget.Toast
import com.example.mapsdemo.map_screen.MapsActivity
import java.sql.Time
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.sqrt

class AccelerometerListener : SensorEventListener {
    var bumpDetected : Boolean = false
    val THRESHOLD = 100
    val BUMP_DURATION_MILLISECONDS = 5000L

    override fun onSensorChanged(event: SensorEvent?) {
        // Handle accelerometer sensor data here
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val acceleration = sqrt(x*x + y*y + z*z)

            if (acceleration > THRESHOLD && !bumpDetected) {
                bumpDetected = true
                Timer().schedule(BUMP_DURATION_MILLISECONDS){
                    bumpDetected = false
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }
}
