package com.example.mapsdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "not_saved_speed_camera")
data class NotSavedSpeedCamera(var latitude : Double,
                               var longitude : Double,
                               var radius : Double,
                               @PrimaryKey val id : String,
                                var creator : String)
