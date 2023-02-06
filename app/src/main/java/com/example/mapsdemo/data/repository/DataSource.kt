package com.example.mapsdemo.data.repository

import com.example.mapsdemo.data.model.Bump
import com.example.mapsdemo.data.model.NotificationStatus

interface DataSource {
    suspend fun saveBump(bump: Bump)
    suspend fun saveAllBumps (bumps : Bump)
    suspend fun getAllBumps() : List<Bump>
    suspend fun updateNotificationStatus(notificationStatus: NotificationStatus)
    suspend fun getNotificationStaus() : Boolean
    suspend fun clear()
}