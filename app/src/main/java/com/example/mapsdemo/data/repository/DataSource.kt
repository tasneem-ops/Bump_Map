package com.example.mapsdemo.data.repository

import com.example.mapsdemo.data.model.Bump
import com.example.mapsdemo.data.model.NotificationStatus
import com.example.mapsdemo.data.model.SpeedCamera

interface DataSource {
    suspend fun saveBump(bump: Bump)
    suspend fun saveAllBumps (bumps : Bump)
    suspend fun getAllBumps() : List<Bump>
    suspend fun clear()
    suspend fun refreshBumps(): List<Bump>
    suspend fun getBumpById(id : String): Bump?
    suspend fun clearBumpsCache()
    suspend fun getCahcedBumps() : List<Bump>
    suspend fun saveCachedBump(bump: Bump)

    suspend fun getAllSpeedCamera(): List<SpeedCamera>
    suspend fun getSpeedCameraById(id : String): SpeedCamera?
    suspend fun insertSpeedCamera(speedCamera: SpeedCamera)
}