package com.rdt.bleservice

import android.content.Context
import java.util.*

class MyConfig {

    companion object {
        const val DEBUG = true
        const val HOME_PATH = "rdtone"
        const val DEVICE_NAME = "RDTONE"
        const val DATE_TIME_PATTERN = "yyyy-MM-dd_HH-mm-ss"
        const val EXTRAS_DEVICE_NAME = "DEVICE_NAME"
        const val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"
        const val SCAN_PERIOD = 1000L
        const val LIST_NAME = "NAME"
        const val LIST_UUID = "UUID"

        var zContext: Context? = null
        var zConnectedDeviceName: String? = null
        var zConnectedDeviceAddress: String? = null
        fun getConnectedDeviceInfo() {
            if (zContext != null) {
                val prefs = zContext?.getSharedPreferences(BLEKey.DEVICE_INFO.s, Context.MODE_PRIVATE)
                if (prefs != null) {
                    zConnectedDeviceAddress = prefs.getString(BLEKey.DEVICE_ADDRESS.s, null)
                    zConnectedDeviceName = prefs.getString(BLEKey.DEVICE_NAME.s, null)
                }
            }
        }
        fun setConnectedDeviceInfo() {
            if (zContext != null) {
                val prefs = zContext?.getSharedPreferences(BLEKey.DEVICE_INFO.s, Context.MODE_PRIVATE)
                if (prefs != null) {
                    val editor = prefs.edit()
                    editor.putString(BLEKey.DEVICE_ADDRESS.s, zConnectedDeviceAddress)
                    editor.putString(BLEKey.DEVICE_NAME.s, zConnectedDeviceName)
                    editor.apply()
                }
            }
        }
        fun resetConnectedDeviceInfo() {
            zConnectedDeviceAddress = null
            zConnectedDeviceName = null
        }
    }

}

/* EOF */