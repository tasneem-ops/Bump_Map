package com.example.mapsdemo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

@Entity (tableName = "bumps")
data class Bump(
    var latitude : Double,
    var longitude : Double,
    var radius : Double,
    @PrimaryKey val id : String,
    var UpVotes : Int,
    var DownVotes : Int
)