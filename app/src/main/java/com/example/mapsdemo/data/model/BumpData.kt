package com.example.mapsdemo.data.model

import androidx.room.PrimaryKey

data class BumpData(var latitude : Double? = null,
                    var longitude : Double? = null,
                    var id : String? = null,
                    var upVotes : Int? = null,
                    var downVotes :Int? = null,
                    var creator : String? = null)
