package com.example.mapsdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "not_saved_bumps")
data class NotSavedBumps(var latitude : Double,
                         var longitude : Double,
                         var radius : Double,
                         @PrimaryKey val id : String)
