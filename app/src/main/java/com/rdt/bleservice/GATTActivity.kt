package com.rdt.bleservice

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.rdt.bleservice.MyConfig.Companion.EXTRAS_DEVICE_ADDRESS
import com.rdt.bleservice.MyConfig.Companion.EXTRAS_DEVICE_NAME
import com.rdt.bleservice.MyConfig.Companion.LIST_NAME
import com.rdt.bleservice.MyConfig.Companion.LIST_UUID
import com.rdt.bleservice.MyGATTAttributes.Companion.HM_RX_TX
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GATTActivity : AppCompatActivity() {

    companion object {
        fun newInstance(context: Context): Intent {
            return Intent(context, GATTActivity::class.java)
        }
    }
    private val TAG = MainActivity::class.java.simpleName
    private var mService: BLEService? = null
    private lateinit var mDeviceName: String
    private lateinit var mDeviceAddress: String
    private var mConnected = false
    private val mNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.xNavigationX -> {
                //xMessage.setText(R.string.title_x)
                mService!!.disconnect()
                return@OnNavigationItemSelectedListener true
            }
            R.id.xNavigationY -> {
                //xMessage.setText(R.string.title_y)
                mService!!.readCharacteristic(mCharacteristicRx!!)
                return@OnNavigationItemSelectedListener true
            }
            R.id.xNavigationZ -> {
                //xMessage.setText(R.string.title_z)
                sendData()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mService = (service as BLEService.MyBinder).getService()
            mService!!.initialize()
            mService!!.connect(mDeviceAddress)
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
        }
    }
    private val mGattAttributes = MyGATTAttributes()
    private var mCharacteristicTx: BluetoothGattCharacteristic? = null
    private var mCharacteristicRx: BluetoothGattCharacteristic? = null
    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                GATTAction.GATT_CONNECTED.s -> {
                    mConnected = true
                    updateConnectionState(R.string.connected)
                }
                GATTAction.GATT_DISCONNECTED.s -> {
                    mConnected = false
                    updateConnectionState(R.string.disconnected)
                }
                GATTAction.GATT_SERVICES_DISCOVERED.s -> {
                    displayGattService(mService?.getSupportedGattServices())
                }
                GATTAction.GATT_DATA_AVAILABLE.s -> {
                    displayData(intent.getStringExtra(GATTAction.EXTRA_DATA.s))
                }
            }
        }
    }

    //
    // ACTIVITY CALLBACK
    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gatt)
        xNavView.setOnNavigationItemSelectedListener(mNavigationItemSelectedListener)
        val i = intent
        mDeviceName = i.getStringExtra(EXTRAS_DEVICE_NAME)
        mDeviceAddress = i.getStringExtra(EXTRAS_DEVICE_ADDRESS)
        bindService(BLEService.newInstance(this), mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        if (mService != null) {
            mService!!.connect(mDeviceAddress)
        }
    }

    override fun onPause() {
        unregisterReceiver(mGattUpdateReceiver)
        super.onPause()
    }

    override fun onDestroy() {
        unbindService(mServiceConnection)
        super.onDestroy()
    }

    //
    // PRIVATE FUN
    //
    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction(GATTAction.GATT_CONNECTED.s)
        filter.addAction(GATTAction.GATT_DISCONNECTED.s)
        filter.addAction(GATTAction.GATT_SERVICES_DISCOVERED.s)
        filter.addAction(GATTAction.GATT_DATA_AVAILABLE.s)
        return filter
    }

    private fun sendData() {
        val str = "test"
        if (mConnected) {
            mCharacteristicTx!!.value = str.toByteArray()
            mService!!.writeCharacteristic(mCharacteristicTx!!)
            mService!!.setCharacteristicNotification(mCharacteristicRx!!, true)
        }
    }

    private fun updateConnectionState(resourceId: Int) {
        runOnUiThread {
            xMessage.append("\n")
            xMessage.setText(resourceId)
            xMessage.append("\n")
        }
    }

    private fun displayGattService(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid: String? = null
        val unknownService = resources.getString(R.string.unknown_service)
        val services = ArrayList<HashMap<String, String>>()
        for (gattService in gattServices) {
            val currentService = HashMap<String, String>()
            uuid = gattService.uuid.toString()
            currentService[LIST_NAME] = mGattAttributes.lookup(uuid, unknownService)
            currentService[LIST_UUID] = uuid
            services.add(currentService)
            mCharacteristicTx = gattService.getCharacteristic(UUID.fromString(HM_RX_TX))
            mCharacteristicRx = gattService.getCharacteristic(UUID.fromString(HM_RX_TX))
        }
    }

    private fun displayData(data: String?) {
        if (data != null) {
            //xMessage.text = data
            xMessage.append(data)
        }
    }

}

/* EOF */