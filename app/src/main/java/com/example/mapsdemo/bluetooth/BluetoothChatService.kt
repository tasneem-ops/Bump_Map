package com.example.mapsdemo.bluetooth

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


const val MESSAGE_READ: Int = 0
const val MESSAGE_WRITE: Int = 1
const val MESSAGE_TOAST: Int = 2
class BluetoothChatService() {
    private val TAG = "BluetoothChatService"
    private val NAME = "BluetoothApp"
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private lateinit var mContext : Context
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var mHandler: Handler

    private var connectThread : ConnectThread? = null
    private var acceptThread : AcceptThread? = null
    private var connectedThread : ConnectedThread? = null

    private lateinit var mProgressDialog: ProgressDialog
    public constructor(context: Context, bluetoothAdapter: BluetoothAdapter, handler: Handler) : this() {
        mContext = context
        mBluetoothAdapter = bluetoothAdapter
        mHandler = handler
        start()
    }

    @SuppressLint("MissingPermission")
    private inner class AcceptThread : Thread() {

        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            mBluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID)
        }

        override fun run() {
            // Keep listening until exception occurs or a socket is returned.
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }
                socket?.also {
                    manageMyConnectedSocket(it)
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        fun cancel() {
            try {
                mmServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private inner class ConnectThread(device: BluetoothDevice) : Thread() {

        private val mmSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(MY_UUID)
        }

        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            mBluetoothAdapter?.cancelDiscovery()

            mmSocket?.let { socket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                try {
                    socket.connect()
                }
                catch (e:Exception){
                    Log.d(TAG, "${e.message}")
                    val bundle = Bundle()
                    bundle.putString("error", e.message)
                    val message = Message()
                    message.data= bundle
                    mHandler.sendMessage(message)
                }
                //mProgressDialog.dismiss()
                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                manageMyConnectedSocket(socket)
            }
        }

        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the client socket", e)
            }
        }
    }
    @Synchronized
    public fun start(){
        if(connectThread != null){
            connectThread?.cancel()
            connectThread = null
        }
        if(acceptThread ==null){
            acceptThread = AcceptThread()
            acceptThread?.start()
        }
    }

    public fun startClient(device: BluetoothDevice, uuid: UUID){
        Log.d(TAG, "startClient: Started!")
       // mProgressDialog = ProgressDialog.show(mContext, "Connecting", "Please Wait...",true)
        connectThread = ConnectThread(device)
        connectThread?.start()
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {

        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream
        private var incomingMessage : String? = null
        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }

                incomingMessage = String(mmBuffer, 0, numBytes)
                Log.d(TAG, "Input Stream: ${incomingMessage}")
                val message = Message()
                val bundle = Bundle()
                bundle.putString("msg", incomingMessage)
                message.data = bundle
                mHandler.sendMessage(message)
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }


    private fun manageMyConnectedSocket(bluetoothSocket: BluetoothSocket) {
        Log.d(TAG, "connected")
        connectedThread = ConnectedThread(bluetoothSocket)
        connectedThread?.start()
    }
    public fun write(out : ByteArray){
        connectedThread?.write(out)
    }

}