package com.example.mapsdemo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mapsdemo.data.model.Bump
import com.example.mapsdemo.data.model.NotificationStatus

@Dao
interface BumpDao{
    @Query("select * from bumps")
    suspend fun getAllBumps() : List<Bump>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bump: Bump)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBumps(vararg bumps : Bump)

    @Query ("DELETE FROM bumps")
    suspend fun clear()

}

@Dao
interface NotificationStatusDao{
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStatus(status: NotificationStatus)
    @Query("select * from notification_status")
    suspend fun getNotificationStatus(): NotificationStatus
}