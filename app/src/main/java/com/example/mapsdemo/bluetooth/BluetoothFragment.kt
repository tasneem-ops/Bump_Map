package com.example.mapsdemo.bluetooth

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.mapsdemo.BuildConfig
import com.example.mapsdemo.R
import com.example.mapsdemo.data.model.Bluetooth_Device
import com.example.mapsdemo.databinding.FragmentBluetoothBinding
import com.example.mapsdemo.main_screen.WelcomeFragmentDirections
import com.example.mapsdemo.map_screen.MapsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import java.security.Permission
import java.util.*

class BluetoothFragment : Fragment() {
    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        var m_bluetoothAdapter: BluetoothAdapter? = null
        var m_isConnected: Boolean = false
        var m_address: String? = null
        const val REQUEST_ENABLE_BT = 1
        const val REQUEST_BT_PERMISSION = 2
        var connected_device_type : String? = null

    }
    private lateinit var adapter: BluetoothDeviceListAdapter
    private lateinit var binding : FragmentBluetoothBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBluetoothBinding.inflate(inflater)
        adapter = BluetoothDeviceListAdapter(BluetoothDeviceListListener { bluetoothDevice ->
            Toast.makeText(context, "You Choose: ${bluetoothDevice.name}", Toast.LENGTH_LONG).show()
            m_address = bluetoothDevice.MAC
        })
        binding.devicesRecycler.adapter = adapter
        val bluetoothManager: BluetoothManager = requireContext().getSystemService(BluetoothManager::class.java)
        m_bluetoothAdapter = bluetoothManager!!.getAdapter()
        if (m_bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        checkBluetoothPermission()
        if (m_bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
//        val _pairedDevices: Set<BluetoothDevice>? = m_bluetoothAdapter?.bondedDevices
//        var pairedDevices : MutableList<Bluetooth_Device> = mutableListOf()
//        _pairedDevices?.forEach { device ->
//            pairedDevices.add(Bluetooth_Device(device.name, device.address))
//        }
//        adapter.submitList(pairedDevices)
//        binding.connectBtn.setOnClickListener {
//            if (m_address != null){
//                val device = findDeviceByAddress(m_address!!, _pairedDevices)
//                if (device !=null){
//                    val intent = MapsActivity.newIntent(requireContext(), device)
//                    startActivity(intent)
//                }
//            }
//        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomNavBar = view?.findViewById<BottomNavigationView>(R.id.bottonnav)
        bottomNavBar?.selectedItemId = R.id.bluetooth
        bottomNavBar?.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home ->{
                    findNavController().navigate(BluetoothFragmentDirections.actionBluetoothFragmentToWelcomeFragment())
                    true
                }
                R.id.bluetooth ->{
                    true
                }
                R.id.map_item ->{
                    val intent = MapsActivity.newIntent(requireContext(), null)
                    startActivity(intent)
                    true
                }
                R.id.settings ->{
                    findNavController().navigate(BluetoothFragmentDirections.actionBluetoothFragmentToSettingsFragment())
                    true
                }
                else->{
                    true
                }
            }
        }
    }

    private fun findDeviceByAddress(mAddress: String, _pairedDevices: Set<BluetoothDevice>?): BluetoothDevice? {
        _pairedDevices?.forEach {
            if (it.address == mAddress){
                return it
            }
        }
        return null
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK ){
            val _pairedDevices: Set<BluetoothDevice>? = m_bluetoothAdapter?.bondedDevices
            var pairedDevices : MutableList<Bluetooth_Device> = mutableListOf()
            _pairedDevices?.forEach { device ->
                pairedDevices.add(Bluetooth_Device(device.name, device.address))
            }
            adapter.submitList(pairedDevices)
        }
    }

    private fun checkBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                REQUEST_BT_PERMISSION)
        } else {
            // Permission has already been granted
            // Start Bluetooth enablement activity here
            if (m_bluetoothAdapter?.isEnabled == false) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
            val _pairedDevices: Set<BluetoothDevice>? = m_bluetoothAdapter?.bondedDevices
            var pairedDevices : MutableList<Bluetooth_Device> = mutableListOf()
            _pairedDevices?.forEach { device ->
                pairedDevices.add(Bluetooth_Device(device.name, device.address))
            }
            adapter.submitList(pairedDevices)
            binding.connectBtn.setOnClickListener {
                if (m_address != null){
                    val device = findDeviceByAddress(m_address!!, _pairedDevices)
                    if (device !=null){
                        val intent = MapsActivity.newIntent(requireContext(), device)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_BT_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted, start Bluetooth enablement activity here
                if (m_bluetoothAdapter?.isEnabled == false) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                }
                val _pairedDevices: Set<BluetoothDevice>? = m_bluetoothAdapter?.bondedDevices
                var pairedDevices : MutableList<Bluetooth_Device> = mutableListOf()
                _pairedDevices?.forEach { device ->
                    pairedDevices.add(Bluetooth_Device(device.name, device.address))
                }
                adapter.submitList(pairedDevices)
            } else {
                // Permission has been denied, show a message or take appropriate action
                Snackbar.make(
                    view!!,
                    R.string.bluetooth_permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
            }
        }
    }
//
//    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
//        private var connectSuccess: Boolean = true
//        private val context: Context
//
//        init {
//            this.context = c
//        }
//
//        override fun onPreExecute() {
//            super.onPreExecute()
//            m_progress = ProgressDialog.show(context, "Connecting...", "please wait")
//        }
//
//        @SuppressLint("MissingPermission")
//        override fun doInBackground(vararg p0: Void?): String? {
//            try {
//                if (m_bluetoothSocket == null || !m_isConnected) {
//                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
//                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
//                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
//                    m_bluetoothSocket!!.connect()
//                }
//            } catch (e: IOException) {
//                connectSuccess = false
//                e.printStackTrace()
//            }
//            return null
//        }
//
//        override fun onPostExecute(result: String?) {
//            super.onPostExecute(result)
//            if (!connectSuccess) {
//                Log.i("data", "couldn't connect")
//            } else {
//                m_isConnected = true
//                if (connected_device_type == "embedded"){
//                    BluetoothService.embeddedSocket = m_bluetoothSocket
//                }
//                if (connected_device_type == "ai"){
//                    BluetoothService.aiSocket = m_bluetoothSocket
//                }
//            }
//            m_progress.dismiss()
//        }
//    }
}