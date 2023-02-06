package com.example.mapsdemo

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mapsdemo.data.local.BumpDatabase
import com.example.mapsdemo.data.model.Bump
import com.example.mapsdemo.data.repository.LocalRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainViewModel(application: Application)
    : AndroidViewModel(application) {
    val database = BumpDatabase.getInstance(application)
    val repository = LocalRepository(database)

    private val _bumps = MutableLiveData<List<Bump>>()
    val bumps : LiveData<List<Bump>>
    get() = _bumps

    private val _isUnique = MutableLiveData<Boolean>()
    val isUnique : LiveData<Boolean>
    get() = _isUnique

    fun saveBump(bump: Bump){
        if(validateBumpData(bump)){
            viewModelScope.launch {
                repository.saveBump(bump)
            }
        }

    }
    fun validateBumpData(bump: Bump): Boolean{
        if(bump.latitude !=null && bump.longitude!=null){
            return true
        }
        return false
    }


    fun getAllbumps(){
        viewModelScope.launch {
           _bumps.value = repository.getAllBumps()
        }
    }

    fun validateUniqueBump(newBump: Bump) {
        var allBumps : List<Bump>
        runBlocking{
            allBumps = repository.getAllBumps()
            if(allBumps !=null){
                var x = 0
                for (bump in allBumps){
                    if (calcDistance(newBump.latitude, bump.latitude, newBump.longitude, bump.longitude)<20){
                        Log.d("Geofence", "Bump is not unique, NOT ADDED!")
                        _isUnique.value = false
                        x = 1
                        break
                    }
                }
                if (x == 0){
                    _isUnique.value = true
                }
            }
            else{
                _isUnique.value = true
            }

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

}