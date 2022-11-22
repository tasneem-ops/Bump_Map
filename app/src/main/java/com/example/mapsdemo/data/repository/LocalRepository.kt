package com.example.mapsdemo.data.repository

import com.example.mapsdemo.data.local.BumpDao
import com.example.mapsdemo.data.model.Bump
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalRepository(private val bumpDao: BumpDao,
                        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : DataSource {
    override suspend fun saveBump(bump: Bump) =
        withContext(ioDispatcher) {
            bumpDao.insert(bump)
    }

    override suspend fun saveAllBumps(bumps: Bump)=
        withContext(ioDispatcher){
            bumpDao.insertAllBumps(bumps)
    }

    override suspend fun getAllBumps(): List<Bump> =
        withContext(ioDispatcher){
        bumpDao.getAllBumps()
    }
}