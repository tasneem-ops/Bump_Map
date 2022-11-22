package com.example.mapsdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity (tableName = "bumps")
data class Bump(
    var latitude : Double,
    var longitude : Double,
    @PrimaryKey val id: String = UUID.randomUUID().toString()
)