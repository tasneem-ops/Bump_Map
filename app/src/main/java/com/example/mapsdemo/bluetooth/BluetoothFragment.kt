package com.example.mapsdemo.bluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.example.mapsdemo.R
import com.example.mapsdemo.data.model.Bluetooth_Device
import com.example.mapsdemo.databinding.FragmentBluetoothBinding
import com.example.mapsdemo.map_screen.MapsActivity
import kotlinx.android.synthetic.*
import java.io.IOException
import java.util.*

class BluetoothFragment : Fragment() {
    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        var m_address: String? = null
        const val REQUEST_ENABLE_BT = 1
        var connected_device_type : String? = null
        var m_bluetoothChatService : BluetoothChatService? = null
    }
    private lateinit var adapter: BluetoothDeviceListAdapter
    private lateinit var binding : FragmentBluetoothBinding
    val handler = object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Toast.makeText(requireContext(), msg?.data?.getString("msg"), Toast.LENGTH_SHORT).show()
            Toast.makeText(requireContext(), msg.data.getString("error"), Toast.LENGTH_SHORT).show()
        }
    }
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
        val bluetoothChatService = BluetoothChatService(requireContext(), m_bluetoothAdapter!!, handler)
        m_bluetoothChatService = bluetoothChatService
        binding.connectBtn.setOnClickListener {
            if (m_address != null){
                val device = findDeviceByAddress(m_address!!, _pairedDevices)
                if (device !=null){
                    bluetoothChatService.startClient(device, m_myUUID)
                }
            }
        }
        binding.fab.setOnClickListener{
            val intent = Intent(requireContext(), MapsActivity::class.java)
            startActivity(intent)
        }
        return binding.root
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

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "please wait")
        }

        @SuppressLint("MissingPermission")
        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                m_isConnected = true
                if (connected_device_type == "embedded"){
                    BluetoothService.embeddedSocket = m_bluetoothSocket
                }
                if (connected_device_type == "ai"){
                    BluetoothService.aiSocket = m_bluetoothSocket
                }
            }
            m_progress.dismiss()
        }
    }
}