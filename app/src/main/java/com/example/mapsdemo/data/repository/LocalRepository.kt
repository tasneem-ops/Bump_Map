package com.example.mapsdemo.data.repository

import com.example.mapsdemo.data.local.BumpDao
import com.example.mapsdemo.data.local.BumpDatabase
import com.example.mapsdemo.data.local.NotificationStatusDao
import com.example.mapsdemo.data.model.Bump
import com.example.mapsdemo.data.model.NotificationStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalRepository(val database: BumpDatabase,
                        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : DataSource {

    override suspend fun saveBump(bump: Bump) =
        withContext(ioDispatcher) {
            database.bumpDao.insert(bump)
    }

    override suspend fun saveAllBumps(bumps: Bump)=
        withContext(ioDispatcher){
            database.bumpDao.insertAllBumps(bumps)
    }

    override suspend fun getAllBumps(): List<Bump> =
        withContext(ioDispatcher){
        database.bumpDao.getAllBumps()
    }

    override suspend fun updateNotificationStatus(notificationStatus: NotificationStatus) {
        withContext(ioDispatcher){
            database.notificationStatusDao.updateStatus(notificationStatus)
        }
    }

    override suspend fun getNotificationStaus(): Boolean {
        var status : Boolean
        withContext(ioDispatcher){
            val notificationStatus = database.notificationStatusDao.getNotificationStatus()
            status = notificationStatus.status
        }
        return status
    }

    override suspend fun clear() {
        withContext(ioDispatcher){
            database.bumpDao.clear()
        }
    }
}