package com.example.mapsdemo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mapsdemo.data.model.Bump

@Dao
interface BumpDao{
    @Query("select * from bumps")
    suspend fun getAllBumps() : List<Bump>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bump: Bump)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBumps(vararg bumps : Bump)

}