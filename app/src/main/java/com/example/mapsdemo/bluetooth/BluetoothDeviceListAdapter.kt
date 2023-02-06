package com.example.mapsdemo.bluetooth

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mapsdemo.data.model.Bluetooth_Device
import com.example.mapsdemo.databinding.BluetoothDeviceItemBinding


class BluetoothDeviceListAdapter(val clickListener : BluetoothDeviceListListener) : ListAdapter<Bluetooth_Device, BluetoothDeviceListAdapter.BluetoothDeviceViewHolder>(DiffCallback){
    companion object DiffCallback: DiffUtil.ItemCallback <Bluetooth_Device>(){
        override fun areItemsTheSame(oldItem: Bluetooth_Device, newItem: Bluetooth_Device): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Bluetooth_Device, newItem: Bluetooth_Device): Boolean {
            return oldItem.name == newItem.name
        }
    }
    class BluetoothDeviceViewHolder(private var binding : BluetoothDeviceItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(clickListener: BluetoothDeviceListListener, book: Bluetooth_Device){
            binding.bluetoothDevice = book
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothDeviceViewHolder {
        return BluetoothDeviceViewHolder(BluetoothDeviceItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: BluetoothDeviceViewHolder, position: Int) {
        val bluetoothDevice =  getItem(position)
        holder.bind(clickListener,bluetoothDevice)
    }
}

class BluetoothDeviceListListener(val clickListener: (bluetoothDevice: Bluetooth_Device) -> Unit){
    fun onClick(bluetoothDevice: Bluetooth_Device) = clickListener(bluetoothDevice)

}