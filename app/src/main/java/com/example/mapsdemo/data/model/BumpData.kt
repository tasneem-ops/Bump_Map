package com.example.mapsdemo.data.model

import androidx.room.PrimaryKey

data class BumpData(var latitude : Double? = null,
                    var longitude : Double? = null,
                    var radius : Double? =null,
                    var id : String? = null)
