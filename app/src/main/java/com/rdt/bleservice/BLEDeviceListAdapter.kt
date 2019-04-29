package com.rdt.bleservice

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.device.view.*

class BLEDeviceListAdapter(context: Context) : BaseAdapter() {

    private val TAG = BLEDeviceListAdapter::class.java.simpleName
    private var mInflater: LayoutInflater = LayoutInflater.from(context)
    private val mDevices = ArrayList<BluetoothDevice>()

    fun addDevice(device: BluetoothDevice) {
        if (!mDevices.contains(device)) {
            mDevices.add(device)
        }
    }

    fun getDevice(position: Int): BluetoothDevice {
        return mDevices[position]
    }

    fun clear() {
        mDevices.clear()
    }

    //
    // BASE ADAPTER CALLBACK
    //
    override fun getCount(): Int {
        return mDevices.size
    }

    override fun getItem(position: Int): Any {
        return mDevices[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder
        if (convertView == null) {
            view = mInflater.inflate(R.layout.device, parent, false)
            viewHolder = ViewHolder()
            viewHolder.deviceName = view.xDeviceName
            viewHolder.deviceAddress = view.xDeviceAddress
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = convertView.tag as ViewHolder
        }
        val device = mDevices[position]
        if (device.name != null && device.name.isNotEmpty()) {
            viewHolder.deviceName.text = device.name
        } else {
            viewHolder.deviceName.setText(R.string.unknown_device)
        }
        viewHolder.deviceAddress.text = device.address
        return view
    }

    // VIEW HOLDER
    private class ViewHolder {
        lateinit var deviceName: TextView
        lateinit var deviceAddress: TextView
    }

}

/* EOF */