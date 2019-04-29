package com.rdt.bleservice

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.util.*

class BLEService : Service() {

    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, BLEService::class.java)
        }
    }
    private val TAG = BLEService::class.java.simpleName
    private var mConnectionState = GATTState.STATE_DISCONNECTED.i
    private val mBTAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private var mBTDeviceAddress: String? = null
    private var mBTGatt: BluetoothGatt? = null
    private val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = GATTState.STATE_CONNECTED.i
                broadcastGattUpdate(GATTAction.GATT_CONNECTED.s)
                mBTGatt!!.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = GATTState.STATE_DISCONNECTED.i
                broadcastGattUpdate(GATTAction.GATT_DISCONNECTED.s)
            }
        }
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastGattUpdate(GATTAction.GATT_SERVICES_DISCOVERED.s)
            }
        }
        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastGattUpdate(GATTAction.GATT_DATA_AVAILABLE.s, characteristic)
            }
        }
        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            broadcastGattUpdate(GATTAction.GATT_DATA_AVAILABLE.s, characteristic)
        }
    }
    private val mBinder: IBinder = MyBinder()
    inner class MyBinder : Binder() {
        fun getService(): BLEService {
            return this@BLEService
        }
    }

    //
    // PUBLIC FUN
    //
    fun initialize(): Boolean {
        return true
    }

    fun connect(address: String): Boolean {
        if (mBTDeviceAddress != null && address == mBTDeviceAddress && mBTGatt != null) {
            return if (mBTGatt!!.connect()) {
                mConnectionState = GATTState.STATE_CONNECTING.i
                true
            } else {
                val device = mBTAdapter.getRemoteDevice(address)
                mBTGatt = device.connectGatt(this, false, mGattCallback)
                mBTDeviceAddress = address
                false
            }
        }
        val device = mBTAdapter.getRemoteDevice(address)
        return if (device == null) {
            false
        } else {
            mBTGatt = device.connectGatt(this, false, mGattCallback)
            mBTDeviceAddress = address
            mConnectionState = GATTState.STATE_CONNECTING.i
            true
        }
    }

    fun disconnect() {
        if (mBTGatt != null) {
            mBTGatt!!.disconnect()
        }
    }

    fun close() {
        if (mBTGatt != null) {
            mBTGatt!!.close()
            mBTGatt  = null
        }
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (mBTGatt != null) {
            mBTGatt!!.readCharacteristic(characteristic)
        }
    }

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (mBTGatt != null) {
            mBTGatt!!.writeCharacteristic(characteristic)
        }
    }

    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enabled: Boolean) {
        if (mBTGatt != null) {
            mBTGatt!!.setCharacteristicNotification(characteristic, enabled)
        }
        if (UUID.fromString(MyGATTAttributes.HM_RX_TX) == characteristic.uuid) {
            val descriptor = characteristic.getDescriptor(UUID.fromString(MyGATTAttributes.CLIENT_CHARACTERISTIC_CONFIG))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            mBTGatt!!.writeDescriptor(descriptor)
        }
    }

    fun getSupportedGattServices(): List<BluetoothGattService>? {
        return if (mBTGatt != null) {
            mBTGatt!!.services
        } else {
            null
        }
    }

    //
    // PRIVATE FUN
    //
    private fun broadcastGattUpdate(action: String) {
        sendBroadcast(Intent(action))
    }

    private fun broadcastGattUpdate(action: String, characteristic: BluetoothGattCharacteristic?) {
        val intent = Intent(action)
        val data = characteristic?.value
        if (data != null && data.isNotEmpty()) {
            //val str = StringBuilder(data.size)
            //for (b in data) str.append(String.format("%02x ", b))
            intent.putExtra(GATTAction.EXTRA_DATA.s, String.format("%s", String(data)))
        }
        sendBroadcast(intent)
    }

    //
    // SERVICE CALLBACK
    //
    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

}

/* EOF */