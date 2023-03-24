package com.example.mapsdemo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mapsdemo.data.model.Bump
import com.example.mapsdemo.data.model.SpeedCamera

@Dao
interface BumpDao{
    @Query("select * from bumps")
    suspend fun getAllBumps() : List<Bump>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBump(bump: Bump)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBumps(vararg bumps: Bump)

    @Query ("DELETE FROM bumps")
    suspend fun clear()

    @Query ("SELECT * FROM bumps WHERE id =:id")
    suspend fun getBumpById(id : String): Bump?

}
@Dao
interface NotSavedBumpsDao{

    @Query ("DELETE FROM not_saved_bumps")
    suspend fun clearBumpsCache()

    @Query("SELECT * FROM not_saved_bumps")
    suspend fun getCachedBumps() : List<Bump>

    @Insert
    suspend fun saveCachedBump(bump: Bump)
}

@Dao
interface SpeedCameraDao{
    @Query("SELECT * FROM speed_camera WHERE id =:id")
    suspend fun getSpeedCameraByID(id : String): SpeedCamera?

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeedCamera(speedCamera: SpeedCamera)

    @Query("SELECT * FROM speed_camera")
    suspend fun getAllSpeedCamera(): List<SpeedCamera>
}