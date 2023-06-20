package com.example.mapsdemo.map_screen

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.mapsdemo.AccelerometerListener
import com.example.mapsdemo.BuildConfig
import com.example.mapsdemo.MainViewModel
import com.example.mapsdemo.R
import com.example.mapsdemo.bluetooth.BluetoothChatService
import com.example.mapsdemo.bluetooth.BluetoothFragment
import com.example.mapsdemo.bluetooth.BluetoothFragment.Companion.m_myUUID
import com.example.mapsdemo.data.model.*
import com.example.mapsdemo.databinding.ActivityMapsBinding
import com.example.mapsdemo.geofence.GeofencesUtilFunctions
import com.example.mapsdemo.geofence.GeofencingConstants
import com.example.mapsdemo.main_screen.MainActivity
import com.example.mapsdemo.main_screen.WelcomeFragment
import com.example.mapsdemo.utils.BumpSavedNotification
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.math.sqrt


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
    private lateinit var bumpsDatabaseReference: DatabaseReference
    private lateinit var speedCameraDatabaseReference: DatabaseReference

    private var bluetoothDevice : BluetoothDevice? = null
    var bumps = arrayListOf<Bump>()
    var speedCameras = arrayListOf<SpeedCamera>()
    private lateinit var sensorManager : SensorManager
    private lateinit var accelerometerSensor : Sensor
    val listener = object: SensorEventListener {
        var bumpDetected : Boolean = false
        val THRESHOLD = 30
        val BUMP_DURATION_MILLISECONDS = 5000L
        override fun onSensorChanged(event: SensorEvent?) {
            // Handle accelerometer sensor data here
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val acceleration = sqrt(x*x + y*y + z*z)

                if (acceleration > THRESHOLD && !bumpDetected) {
                    bumpDetected = true
                    Timer().schedule(BUMP_DURATION_MILLISECONDS){
                        bumpDetected = false
                    }
                    onBumpDetected()
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

        }
    }
        val handler = object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Toast.makeText(applicationContext, msg?.data?.getString("msg"), Toast.LENGTH_SHORT).show()
//            Toast.makeText(applicationContext, msg.data.getString("error"), Toast.LENGTH_SHORT).show()
            if (msg.data.getString("msg") == "y"){
                addBump()
                BumpSavedNotification(applicationContext)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, this);
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val viewModelFactory = ViewModelFactory(application )
        viewModel = ViewModelProvider(this , viewModelFactory).get(MainViewModel::class.java)
        geofencesUtilFunctions = GeofencesUtilFunctions(applicationContext, this)
        bumpsDatabaseReference = FirebaseDatabase.getInstance().getReference("Bumps")
        speedCameraDatabaseReference = FirebaseDatabase.getInstance().getReference("SpeedCamera")
        sensorManager.registerListener(listener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        if (BluetoothFragment.m_bluetoothAdapter != null){
            val bluetoothChatService = BluetoothChatService(applicationContext, BluetoothFragment.m_bluetoothAdapter!!, handler)
            m_bluetoothChatService = bluetoothChatService
        }
        bluetoothDevice  = intent.getParcelableExtra(EXTRA_BluetoothDevice)
        if (bluetoothDevice!= null){
            m_bluetoothChatService?.startClient(bluetoothDevice!!, m_myUUID)
        }
        binding.addBump.setOnClickListener {
            addBump()
        }
//        binding.addSpeedCamera.setOnClickListener {
//            addSpeedCamera()
//        }
        refreshData()

//        refreshBumpList()
        //Observe changes to bumps list and Update Geofences
        viewModel.bumps.observe(this, Observer {
            geofencesUtilFunctions.removeGeofences()
            it?.forEach {
                geofencesUtilFunctions.addGeofence(it.latitude, it.longitude, it.radius, it.id)
                bumps.add(it)
            }
        })

        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottonnav)
        bottomNavBar?.selectedItemId = R.id.map_item
        bottomNavBar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home ->{
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.putExtra("Fragment", "home")
                    startActivity(intent)
                    true
                }
                R.id.bluetooth ->{
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.putExtra("Fragment", "bluetooth")
                    startActivity(intent)
                    true
                }
                R.id.map_item ->{
                    true
                }
                R.id.settings ->{
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.putExtra("Fragment", "settings")
                    startActivity(intent)
                    true
                }
                else->{
                    true
                }
            }
        }
    }

    private fun addSpeedCamera() {
        if(foregroundAndBackgroundLocationPermissionApproved() && (latitude!= null) && (longitude!=null)){
            val id  = speedCameraDatabaseReference.push().key
            val speedCamera = SpeedCamera(latitude!! , longitude!!, GeofencingConstants.CAMERA_GEOFENCE_RADIUS_IN_METERS.toDouble(), id!!)
            if(viewModel.validateLocationData(speedCamera)&& viewModel.validateUniqueSpeedCamera(speedCamera, speedCameras)){
                speedCameraDatabaseReference.child(id!!).setValue(speedCamera)
                    .addOnSuccessListener {
                        Toast.makeText(
                            applicationContext,
                            "Speed Camera is Successfully Uploaded!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener { err ->
                        Toast.makeText(
                            applicationContext,
                            "Failed to Upload Data",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            else{
                Toast.makeText(applicationContext, "Speed Camera data is not valid", Toast.LENGTH_SHORT).show()
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.show_bump_marks ->{
                showBumpMarks()
                true
            }
            R.id.remove_bump_marks->{
                removeBumpMarks()
                true
            }
            R.id.update_database->{
                bumps.forEach {
                    viewModel.saveBump(it)
                }
                speedCameras.forEach {
                    viewModel.saveSpeedCamera(it)
                }
                true
            }
            R.id.stop_location_tracking ->{

                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    private fun showBumpMarks() {
        Log.d("MapsActivity", "Bumps Data:" +viewModel.bumps.value.toString())
        bumps.forEach{
            mMap.addMarker(MarkerOptions()
                .position(LatLng(it.latitude, it.longitude)))
                ?.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.bump_notice))
        }
        mMap.setOnMarkerClickListener(GoogleMap.OnMarkerClickListener { marker ->
            val snackbar = Snackbar.make(findViewById(R.id.map), "Is there a bump here?", Snackbar.LENGTH_LONG)
                .setAction("No") {
                    // Responds to click on the action
                    val position = marker.position
                    bumps.forEach { bump ->
                        if (bump.latitude == position.latitude && bump.longitude == position.longitude){
                            val id = bump.id
                            var downVotes = bump.DownVotes
                            var upVotes = bump.UpVotes
                            downVotes ++
                            Toast.makeText(this, "Data Updated : $downVotes and olad data ${bump.DownVotes}", Toast.LENGTH_SHORT).show()
                            if (downVotes - upVotes >= 5){
                                bumpsDatabaseReference.child(id).setValue(null)
                                marker.remove()
                                Toast.makeText(applicationContext, "Bump is removed", Toast.LENGTH_LONG).show()
                            }
                            val updatedBumpData = bump
                            updatedBumpData.DownVotes = downVotes
                            bumpsDatabaseReference.child(id).setValue(updatedBumpData)
                            Toast.makeText(this, "Data Updated : $downVotes", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            .show()


            false
        })
    }

    private fun removeBumpMarks(){
        mMap.clear()
    }
    fun onBumpDetected(){
        Toast.makeText(applicationContext, "Bump is Detected ", Toast.LENGTH_LONG).show()
        BumpSavedNotification(applicationContext)
    }

    private fun addBump() {
        if(foregroundAndBackgroundLocationPermissionApproved() && (latitude!= null) && (longitude!=null)){
                val id = bumpsDatabaseReference.push().key!!
                val bump = Bump(latitude!!, longitude!!,
                    GeofencingConstants.GEOFENCE_RADIUS_IN_METERS.toDouble(), id, 0,0)
                if (viewModel.validateBumpData(bump) && viewModel.validateUniqueBump(bump, bumps)) {
                    bumpsDatabaseReference.child(id!!).setValue(bump)
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
                                "Failed to Upload Data",
                                Toast.LENGTH_LONG
                            ).show()
                        }
//                geofencesUtilFunctions.addGeofence(latitude!!, longitude!!)
                }
                else{
                    Toast.makeText(applicationContext, "Bump data is not valid", Toast.LENGTH_SHORT).show()
                }

//            else{
//                val bump = Bump(latitude!!, longitude!!,
//                    GeofencingConstants.GEOFENCE_RADIUS_IN_METERS.toDouble(), getUniqueId().toString())
//                if (viewModel.validateBumpData(bump) && viewModel.validateUniqueBump(bump, bumps)){
//                    viewModel.saveCachedBump(bump)
//                }
//                else{
//                    Toast.makeText(applicationContext, "Bump data is not valid", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//        else{
//            Snackbar.make(
//                findViewById(R.id.map),
//                R.string.canno_add_geofence,
//                Snackbar.LENGTH_SHORT
//            ).show()
        }
    }
    private fun refreshData(){
        if (isConnected()){
            Toast.makeText(applicationContext, "Connected to the Internet", Toast.LENGTH_SHORT).show()
            refreshBumpList()
//            updateFirebaseDatabase()
            refreshSpeedCameraList()
            Timer().schedule(10000){
                bumps.forEach {
                    viewModel.saveBump(it)
                }
                speedCameras.forEach {
                    viewModel.saveSpeedCamera(it)
                }
            }
        }
        else{
            Toast.makeText(applicationContext, "NOT Connected to the Internet", Toast.LENGTH_SHORT).show()
            if (viewModel.getBumpsFromLocalDatabase().isEmpty())
            else{
                bumps = viewModel.getBumpsFromLocalDatabase() as ArrayList<Bump>
                Timer().schedule(5000){
                    bumps.forEach {
                        geofencesUtilFunctions.addGeofence(it.latitude, it.longitude, it.radius, it.id)
                    }
                }
            }
            if (viewModel.getSpeedCamerasFromLocalDatabase().isEmpty())
            else{
                   speedCameras = viewModel.getSpeedCamerasFromLocalDatabase() as ArrayList<SpeedCamera> /* = java.util.ArrayList<com.example.mapsdemo.data.model.SpeedCamera> */
                    Timer().schedule(5000){
                        speedCameras.forEach {
                            geofencesUtilFunctions.addGeofence(it.latitude, it.longitude, it.radius, it.id)
                        }
                    }
            }
        }
    }
    private fun isConnected(): Boolean{
        val connectivityManager = applicationContext.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        val connected = (activeNetwork!= null && activeNetwork.isConnectedOrConnecting)
        return connected
    }

    private fun updateLocalDatabase(){
        if (viewModel.bumps.value != null)
            viewModel.saveAllBumps(viewModel.bumps.value!!)
        else
            Toast.makeText(applicationContext, "Database not updated", Toast.LENGTH_LONG).show()
    }
    private fun refreshBumpList() {
        bumpsDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bumpList = arrayListOf<BumpData>()
                if (snapshot.exists()){
                    geofencesUtilFunctions.removeGeofences()
                    for (snap in snapshot.children){
                        val bump = snap.getValue(BumpData::class.java)
                        bumpList.add(bump!!)
                        geofencesUtilFunctions
                            .addGeofence(bump.latitude!!, bump.longitude!!, bump.radius!!, bump.id!!)
                        bumps.add(Bump(bump.latitude!!, bump.longitude!!, bump.radius!!, bump.id!!, bump.upVotes!!, bump.downVotes!!))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun updateFirebaseDatabase(){
        bumpsDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bumpList = arrayListOf<BumpData>()
                if (snapshot.exists()){
                    geofencesUtilFunctions.removeGeofences()
                    for (snap in snapshot.children){
                        val bump = snap.getValue(OldBump::class.java)
                        val newBump = BumpData(bump?.latitude, bump?.longitude, bump?.radius, bump?.id, 0,0)
                        bumpsDatabaseReference.child(bump?.id!!).setValue(newBump)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(listener)
    }

    private fun refreshSpeedCameraList(){
        speedCameraDatabaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (snap in snapshot.children){
                        val speedCamera = snap.getValue(SpeedCameraData::class.java)
                        geofencesUtilFunctions.addGeofence(speedCamera?.latitude!!,speedCamera?.longitude!!, speedCamera?.radius!!,
                        speedCamera?.id!!)
                        speedCameras.add(SpeedCamera(speedCamera.latitude!!, speedCamera.longitude!!, speedCamera.radius!!, speedCamera.id!!))
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
                    val zoom = 16.5f
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
