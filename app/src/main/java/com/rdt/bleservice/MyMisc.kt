package com.rdt.bleservice

enum class RequestCode(val i: Int) {
    PERMISSION(101),
    BLUETOOTH_ENABLE(102),
    CONNECT_DEVICE(103)
}

enum class BLEKey(val s: String) {
    DEVICE_INFO("device_info"),
    DEVICE_NAME("device_name"),
    DEVICE_ADDRESS("device_address"),
    TOAST("toast")
}

enum class GATTAction(val s: String) {
    GATT_CONNECTED("ACTION_GATT_CONNECTED"),
    GATT_DISCONNECTED("ACTION_GATT_DISCONNECTED"),
    GATT_SERVICES_DISCOVERED("ACTION_GATT_SERVICES_DISCOVERED"),
    GATT_DATA_AVAILABLE("ACTION_GATT_DATA_AVAILABLE"),
    EXTRA_DATA("EXTRA_DATA")
}

enum class GATTState(val i: Int) {
    STATE_DISCONNECTED(0),
    STATE_CONNECTING(1),
    STATE_CONNECTED(2)
}

/* EOF */