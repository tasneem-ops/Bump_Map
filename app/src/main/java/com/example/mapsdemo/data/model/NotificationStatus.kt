package com.example.mapsdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "notification_status")
data class NotificationStatus( @PrimaryKey val id : Int = 0,
                               val status : Boolean)