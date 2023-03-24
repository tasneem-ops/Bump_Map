package com.example.mapsdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "speed_camera")
data class SpeedCamera(var latitude : Double,
                       var longitude : Double,
                       var radius : Double,
                       @PrimaryKey val id : String)
