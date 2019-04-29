package com.rdt.bleservice

class MyGATTAttributes {

    companion object {
        var CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
        var HM_10_CONF = "0000ffe0-0000-1000-8000-00805f9b34fb"
        var HM_RX_TX = "0000ffe1-0000-1000-8000-00805f9b34fb"
    }
    private val attributes = HashMap<String, String>()
    init {
        attributes[HM_10_CONF] = "HM 10 Serial"
        attributes["00001800-0000-1000-8000-00805f9b34fb"] = "Device Information Service"
        attributes[HM_RX_TX] = "RX/TX data"
        attributes["00002a29-0000-1000-8000-00805f9b34fb"] = "Manufacturer Name String"
    }

    fun lookup(uuid: String, defaultName: String): String {
        val name = attributes[uuid]
        return name ?: defaultName
    }

}

/* EOF */