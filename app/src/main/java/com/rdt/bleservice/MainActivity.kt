package com.rdt.bleservice

import android.app.Activity
import android.app.ListActivity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.PermissionChecker
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.ListView
import com.rdt.bleservice.MyConfig.Companion.EXTRAS_DEVICE_ADDRESS
import com.rdt.bleservice.MyConfig.Companion.EXTRAS_DEVICE_NAME
import com.rdt.bleservice.MyConfig.Companion.SCAN_PERIOD
import com.rdt.bleservice.MyConfig.Companion.zContext
import com.rdt.bleservice.MyUtil.Companion.showToast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : ListActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private lateinit var mPermissions: Array<String>
    private lateinit var mHandler: Handler
    private val mBluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private lateinit var mBluetoothLeScanner: BluetoothLeScanner
    private lateinit var mDeviceListAdapter: BLEDeviceListAdapter
    private var mScanning: Boolean = false
    private val mNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.xNavigationX -> {
                xMessage.setText(R.string.title_x)
                return@OnNavigationItemSelectedListener true
            }
            R.id.xNavigationY -> {
                xMessage.setText(R.string.title_y)
                return@OnNavigationItemSelectedListener true
            }
            R.id.xNavigationZ -> {
                xMessage.setText(R.string.title_z)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
    private val mScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result?.device != null) {
                val device = result!!.device
                mDeviceListAdapter.addDevice(device)
            }
            //super.onScanResult(callbackType, result)
        }
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
        }
        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }
    }

    //
    // ACTIVITY CALLBACK
    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        xNavView.setOnNavigationItemSelectedListener(mNavigationItemSelectedListener)
        zContext = applicationContext
        mPermissions = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS).requestedPermissions
        mHandler = Handler()
        mDeviceListAdapter = BLEDeviceListAdapter(this)
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
        if (!mBluetoothAdapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, RequestCode.BLUETOOTH_ENABLE.i)
        }
        mBluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
        listAdapter = mDeviceListAdapter
        scanLeDevice(true)
    }

    override fun onPause() {
        scanLeDevice(false)
        mDeviceListAdapter.clear()
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RequestCode.BLUETOOTH_ENABLE.i && resultCode == Activity.RESULT_CANCELED) {
            finish()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != RequestCode.PERMISSION.i) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                showToast(R.string.permissionDenied)
            }
        }
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        val device = mDeviceListAdapter.getDevice(position)
        val intent = GATTActivity.newInstance(this)
        intent.putExtra(EXTRAS_DEVICE_NAME, device.name)
        intent.putExtra(EXTRAS_DEVICE_ADDRESS, device.address)
        if (mScanning) {
            mBluetoothLeScanner.stopScan(mScanCallback)
            mScanning = false
        }
        startActivity(intent)
    }

    //
    // PRIVATE FUN
    //
    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            val runnable = Runnable {
                mScanning = false
                mBluetoothLeScanner.stopScan(mScanCallback)
            }
            mHandler.postDelayed(runnable, SCAN_PERIOD)
            mScanning = true
            mBluetoothLeScanner.startScan(mScanCallback)
        } else {
            mScanning = false
            mBluetoothLeScanner.stopScan(mScanCallback)
        }
    }

    private fun checkPermission(): Boolean {
        val notGrantedList = arrayListOf<String>()
        var askUser = false
        for (permission in mPermissions) {
            if (PermissionChecker.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                notGrantedList.add(permission)
                askUser = askUser || ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
            }
        }
        if (notGrantedList.size > 0) {
            requestPermission(notGrantedList.toArray(arrayOfNulls<String>(notGrantedList.size)), askUser)
        }
        return notGrantedList.size == 0
    }

    private fun requestPermission(permissions: Array<String>, askUser: Boolean) {
        when (askUser) {
            true -> {
                val builder = AlertDialog.Builder(this).create()
                builder.setTitle(R.string.permissionTitle)
                builder.setMessage(getString(R.string.permissionMessage))
                builder.setCancelable(false)
                builder.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok)) {
                        _ , _ -> ActivityCompat.requestPermissions(this, permissions, RequestCode.PERMISSION.i)
                }
            }
        }
    }

}

/* EOF */