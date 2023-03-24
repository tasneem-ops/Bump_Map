package com.example.mapsdemo

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mapsdemo.data.local.BumpDatabase
import com.example.mapsdemo.data.model.Bump
import com.example.mapsdemo.data.model.BumpData
import com.example.mapsdemo.data.model.SpeedCamera
import com.example.mapsdemo.data.repository.LocalRepository
import com.example.mapsdemo.geofence.GeofencesUtilFunctions
import com.example.mapsdemo.map_screen.MapsActivity
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.concurrent.schedule

class MainViewModel(application: Application)
    : AndroidViewModel(application) {
    val database = BumpDatabase.getInstance(application)
    val repository = LocalRepository(database)
    private var _bumps = MutableLiveData<List<Bump>>()
    val bumps : LiveData<List<Bump>>
    get() = _bumps

    private var _cachedBumps = MutableLiveData<List<Bump>>()
    val cachedBumps : LiveData<List<Bump>>
        get() = _cachedBumps



    fun refreshBumps(context: Context, activity: MapsActivity) {
        viewModelScope.launch {
            _bumps.value = repository.refreshBumps()
        }
        Timer().schedule(10000){
            Log.d("MapsActivity", "Bumps Data:" +bumps.value.toString())
            try{
                val bumpsData = _bumps.value
                if (bumps != null && bumps.value!!.isEmpty()){
                    Log.d("MapsActivity", "NULL")
                }
                else{
                    Log.d("MapsActivity", "Bumps are supposed to be added to database")
                    viewModelScope.launch {
                        bumps.value?.forEach {
                            repository.saveBump(it)
                            GeofencesUtilFunctions(context, activity).addGeofence(it.latitude, it.longitude, it.radius, it.id)
                        }
                    }
                }
            }
            catch (e: Exception){
                Log.d("MapsActivity", e.toString())
            }
        }
    }
    fun getBumpsFromLocalDatabase(): List<Bump>{
        viewModelScope.launch {
            _bumps.value = repository.getAllBumps()
        }
        if (_bumps.value != null){
            return bumps.value!!
        }
        else {
            return emptyList()
        }
    }

    fun saveBump(bump: Bump){
        if(validateBumpData(bump)){
            viewModelScope.launch {
                repository.saveBump(bump)
            }
        }
    }
    fun saveAllBumps(bumps: List<Bump>){
        bumps.forEach { bump ->
//            saveBump(Bump(bump.latitude!!, bump.longitude!!, bump.radius!!, bump.id!!))
            saveBump(bump)
        }
    }
    fun validateBumpData(bump: Bump): Boolean{
        if(bump.latitude !=null && bump.longitude!=null){
            return true
        }
        return false
    }

    fun validateUniqueBump(newBump: Bump, bumps: List<Bump>) : Boolean{
            if(bumps !=null){
                for (bump in bumps!!){
                    if (calcDistance(newBump.latitude, bump.latitude!!, newBump.longitude,
                            bump.longitude!!)<40){
                        Log.d("Geofence", "Bump is not unique, NOT ADDED!")
                        return false
                    }
                }
                return true
            }
            else{
                return true
            }
    }

    fun calcDistance(lat1 :Double, lat2 :Double, lng1: Double, lng2: Double) : Double{
        println(lat1)
        val lng1 = Math.toRadians(lng1)
        val lng2 = Math.toRadians(lng2)
        val lat1 = Math.toRadians(lat1)
        val lat2 = Math.toRadians(lat2)
        println(lat1)
        val dlon: Double = lng2 - lng1
        val dlat = lat2 - lat1
        val a = (Math.pow(Math.sin(dlat / 2), 2.0)
                + (Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2.0)))

        val c = 2 * Math.asin(Math.sqrt(a))
        // Radius of earth in kilometers.
        val r = 6371.0

        // calculate the result
        return c * r * 1000
    }

    fun saveSpeedCamera(speedCamera: SpeedCamera){
        viewModelScope.launch {
            repository.insertSpeedCamera(speedCamera)
        }
    }

    fun getSpeedCamerasFromLocalDatabase(): List<SpeedCamera>{
        var speedCameras = MutableLiveData<List<SpeedCamera>>()
        viewModelScope.launch {
            speedCameras.value = repository.getAllSpeedCamera()
        }
        if (speedCameras.value != null){
            Log.d("MapsActivity", "Speed Camera Data: " + speedCameras.value.toString())
            return speedCameras.value!!
        }
        else {
            return emptyList()
        }
    }

    fun validateLocationData(speedCamera: SpeedCamera): Boolean{
        if(speedCamera.latitude !=null && speedCamera.longitude!=null){
            return true
        }
        return false
    }

    fun validateUniqueSpeedCamera(newSpeedCamera: SpeedCamera, speedCameras: List<SpeedCamera>) : Boolean{
        if(speedCameras !=null){
            for (speedCamera in speedCameras!!){
                if (calcDistance(newSpeedCamera.latitude, speedCamera.latitude!!, newSpeedCamera.longitude,
                        speedCamera.longitude!!)<200){
                    Log.d("Geofence", "Speed Camera is not unique, NOT ADDED!")
                    return false
                }
            }
            return true
        }
        else{
            return true
        }
    }


    fun setBumpsList(x: List<Bump>){
        _bumps.value = x
    }

    fun saveCachedBump(bump: Bump){
        viewModelScope.launch {
            repository.saveCachedBump(bump)
        }
    }

    fun clearCache(){
        viewModelScope.launch {
            repository.clearBumpsCache()
        }
    }

    fun getCahcedBumps() : List<Bump> {
        viewModelScope.launch{
            _cachedBumps.value = repository.getCahcedBumps()
        }

        if (_cachedBumps.value != null){
            return _cachedBumps.value!!
        }
        else{
            return emptyList()
        }
    }

}