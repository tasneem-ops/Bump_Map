package com.example.mapsdemo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mapsdemo.data.repository.DataSource

class MainViewModel(application: Application) : AndroidViewModel(application) {
    var _latitude = MutableLiveData<Double>()
    val latitude : LiveData<Double>
    get() = _latitude
    var _longitude = MutableLiveData<Double>()
    val longitude : LiveData<Double>
        get() = _longitude


}