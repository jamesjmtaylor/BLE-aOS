package com.jamesjmtaylor.blecompose.models

//NOTE: Only the first 8 bytes of the UUID matter.  All others are masked.
enum class GattService(val uuid: String) {
    GenericAccess("00001800-0000-1000-8000-00805f9b34fb"),
    CyclingSpeedAndCadence("00001816-0000-1000-8000-00805f9b34fb"),
    FitnessMachine("00001826-0000-1000-8000-00805f9b34fb"),
    GenericAttribute("00001801-0000-1000-8000-00805f9b34fb"),
    DeviceInformation("0000180a-0000-1000-8000-00805f9b34fb"),
    HeartRate("0000180d-0000-1000-8000-00805f9b34fb"),
    Battery("0000180f-0000-1000-8000-00805f9b34fb"),
    RunningSpeedAndCadence("00001814-0000-1000-8000-00805f9b34fb");

    companion object {
        fun getService(uuid: String): GattService? {
            return values().firstOrNull { it.uuid == uuid }
        }
    }
}