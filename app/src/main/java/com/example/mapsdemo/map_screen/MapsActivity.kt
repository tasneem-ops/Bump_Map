package com.example.mapsdemo.map_screen

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mapsdemo.BuildConfig
import com.example.mapsdemo.MainViewModel
import com.example.mapsdemo.R
import com.example.mapsdemo.bluetooth.BluetoothChatService
import com.example.mapsdemo.bluetooth.BluetoothFragment
import com.example.mapsdemo.bluetooth.BluetoothFragment.Companion.m_myUUID
import com.example.mapsdemo.data.model.Bump
import com.example.mapsdemo.data.model.BumpData
import com.google.android.gms.maps.model.LatLng
import com.example.mapsdemo.databinding.ActivityMapsBinding
import com.example.mapsdemo.geofence.GeofencesUtilFunctions
import com.example.mapsdemo.geofence.GeofencingConstants
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.location.*

import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.*
import com.google.firebase.database.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    OnMapsSdkInitializedCallback {
    private lateinit var mMap : GoogleMap
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMapsBinding
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q
    private  var latitude : Double? = null
    private var longitude : Double? = null
    private lateinit var locationManager: LocationManager
    private lateinit var geofencesUtilFunctions : GeofencesUtilFunctions
    private lateinit var databaseReference: DatabaseReference
    val handler = object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Toast.makeText(applicationContext, msg?.data?.getString("msg"), Toast.LENGTH_SHORT).show()
//            Toast.makeText(applicationContext, msg.data.getString("error"), Toast.LENGTH_SHORT).show()
            if (msg.data.getString("msg") != null){
                addBump()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, this);
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val viewModelFactory = ViewModelFactory(application )
        viewModel = ViewModelProvider(this , viewModelFactory).get(MainViewModel::class.java)
        geofencesUtilFunctions = GeofencesUtilFunctions(applicationContext, this)
        databaseReference = FirebaseDatabase.getInstance().getReference("Bumps")
        if (BluetoothFragment.m_bluetoothAdapter != null){
            val bluetoothChatService = BluetoothChatService(applicationContext, BluetoothFragment.m_bluetoothAdapter!!, handler)
            m_bluetoothChatService = bluetoothChatService
        }
        val bluetoothDevice : BluetoothDevice? = intent.getParcelableExtra(EXTRA_BluetoothDevice)
        if (bluetoothDevice!= null){
            m_bluetoothChatService?.startClient(bluetoothDevice, m_myUUID)
        }
        binding.addBump.setOnClickListener {
            addBump()
        }

        refreshBumpList()
    }

    private fun addBump() {
        if(foregroundAndBackgroundLocationPermissionApproved() && (latitude!= null) && (longitude!=null)){

            val id = databaseReference.push().key!!
            val bump = Bump(latitude!!, longitude!!,
                GeofencingConstants.GEOFENCE_RADIUS_IN_METERS.toDouble(), id)
            if (viewModel.validateBumpData(bump) && viewModel.validateUniqueBump(bump)) {
                databaseReference.child(id!!).setValue(bump)
                    .addOnSuccessListener {
                        Toast.makeText(
                            applicationContext,
                            "Bump is Successfully Uploaded!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener { err ->
                        Toast.makeText(
                            applicationContext,
                            "Error: ${err.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                geofencesUtilFunctions.addGeofence(latitude!!, longitude!!)
            }
        }
        else{
            Snackbar.make(
                findViewById(R.id.map),
                R.string.canno_add_geofence,
                Snackbar.LENGTH_SHORT
            )
        }
    }

    private fun refreshBumpList() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bumpList = arrayListOf<BumpData>()
                if (snapshot.exists()){
                    GeofencesUtilFunctions(applicationContext, this@MapsActivity).removeGeofences()
                    viewModel.bumps.clear()
                    for (snap in snapshot.children){
                        val bump = snap.getValue(BumpData::class.java)
                        bumpList.add(bump!!)
                        GeofencesUtilFunctions(applicationContext, this@MapsActivity)
                            .addGeofence(bump.latitude!!, bump.longitude!!)
                        viewModel.bumps.add(bump)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        requestForegroundAndBackgroundLocationPermissions()

    }


    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(29 )
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved()){
            checkDeviceLocationSettings()
            return
        }

        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        Log.d(TAG, "Request foreground only location permission")
        ActivityCompat.requestPermissions(
            this@MapsActivity,
            permissionsArray,
            resultCode
        )
    }
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED))
        {
            Snackbar.make(
                findViewById(R.id.map),
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        } else {
            checkDeviceLocationSettings()
            mMap.setMyLocationEnabled(true)
            getLocation()
            locationUpdates()
        }
    }
    @SuppressLint("MissingPermission")
    private fun checkDeviceLocationSettings(resolve:Boolean = true) {
        val locationRequest = LocationRequest().apply {
            priority = PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(this@MapsActivity,
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    findViewById(R.id.map),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                mMap.setMyLocationEnabled(true)
                getLocation()
                locationUpdates()
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val locationRequest = LocationRequest().apply {
            priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }
        val locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if (locationResult !=null){
                    Log.i(TAG, "Got User's location")
                    latitude = locationResult.locations.get((locationResult.locations.size).minus(1)).latitude
                    longitude = locationResult.locations.get((locationResult.locations.size).minus(1)).longitude
                    Log.i(TAG , "location is $latitude $longitude")
                    val latLng = LatLng(latitude ?: -33.870453, longitude?:151.208755)
                    val zoom = 18f
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
                }
                else{
                    Toast.makeText(applicationContext, "Location result is null", Toast.LENGTH_LONG).show()
                }
            }
        }
        LocationServices.getFusedLocationProviderClient(applicationContext)
            .requestLocationUpdates(locationRequest, locationCallback , Looper.myLooper())
    }
    @SuppressLint("MissingPermission")
    private fun locationUpdates() {
        locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L,
            1f, this)
    }


    override fun onLocationChanged(location: Location) {
        //Log.d(TAG, mMap.toString())
        Log.d(TAG, "Location Changed !!  "+ location.latitude +",  "+ location.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(location.latitude, location.longitude)))
        latitude = location.latitude
        longitude = location.longitude
        Log.d(TAG, "Location" + latitude.toString() + longitude.toString())
    }

    override fun onProviderEnabled(provider: String) {
    }

    override fun onProviderDisabled(provider: String) {
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    companion object{
        private const val EXTRA_BluetoothDevice = "EXTRA_BluetoothDevice"
        var m_bluetoothChatService : BluetoothChatService? = null
        fun newIntent(context: Context, bluetoothDevice: BluetoothDevice?): Intent{
            val intent =Intent(context, MapsActivity::class.java)
            intent.putExtra(EXTRA_BluetoothDevice, bluetoothDevice)
            return intent
        }
    }

    override fun onMapsSdkInitialized(p0: MapsInitializer.Renderer) {

    }


    }


private val REQUEST_LOCATION_PERMISSION = 1
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "MapsActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())