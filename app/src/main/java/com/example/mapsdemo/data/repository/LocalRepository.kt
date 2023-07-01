package com.example.mapsdemo.data.repository

import com.example.mapsdemo.data.local.BumpDao
import com.example.mapsdemo.data.local.BumpDatabase
import com.example.mapsdemo.data.model.Bump
import com.example.mapsdemo.data.model.BumpData
import com.example.mapsdemo.data.model.NotificationStatus
import com.example.mapsdemo.data.model.SpeedCamera
import com.example.mapsdemo.geofence.GeofencesUtilFunctions
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalRepository(val database: BumpDatabase,
                        private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : DataSource {

    var bumps : MutableList<Bump>? = null
    override suspend fun saveBump(bump: Bump) =
        withContext(ioDispatcher) {
            database.bumpDao.insertBump(bump)
    }

    override suspend fun saveAllBumps(bumps:Bump)=
        withContext(ioDispatcher){
            database.bumpDao.insertAllBumps(bumps)
    }

    override suspend fun getAllBumps(): List<Bump> =
        withContext(ioDispatcher){
        database.bumpDao.getAllBumps()
    }


    override suspend fun clear() {
        withContext(ioDispatcher){
            database.bumpDao.clear()
        }
    }

    override suspend fun refreshBumps() : List<Bump>{

            val bumpList = arrayListOf<Bump>()
            val databaseReference = FirebaseDatabase.getInstance().getReference("Bumps")
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (snap in snapshot.children){
                            val bumpData = snap.getValue(BumpData::class.java)
                            val bump = Bump(bumpData?.latitude!!, bumpData?.longitude!!, bumpData?.id!!, bumpData.upVotes!!, bumpData.downVotes!!, bumpData.creator!!)
                            bumpList.add(bump)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
            bumps = bumpList
        return bumpList

    }

    override suspend fun getBumpById(id: String): Bump? = withContext(ioDispatcher){
        database.bumpDao.getBumpById(id)
    }

//    override suspend fun clearBumpsCache() = withContext(ioDispatcher){
//        database.notSavedBumpsDao.clearBumpsCache()
//    }
//
//    override suspend fun getCahcedBumps() : List<Bump> =
//        withContext(ioDispatcher){
//            database.notSavedBumpsDao.getCachedBumps()
//        }
//
//    override suspend fun saveCachedBump(bump: Bump) =
//        withContext(ioDispatcher){
//        database.notSavedBumpsDao.saveCachedBump(bump)
//    }

    override suspend fun getAllSpeedCamera(): List<SpeedCamera> = withContext(ioDispatcher) {
        database.speedCameraDao.getAllSpeedCamera()
    }

    override suspend fun getSpeedCameraById(id: String): SpeedCamera? = withContext(ioDispatcher){
        database.speedCameraDao.getSpeedCameraByID(id)
    }

    override suspend fun insertSpeedCamera(speedCamera: SpeedCamera) = withContext(ioDispatcher){
        database.speedCameraDao.insertSpeedCamera(speedCamera)
    }

}