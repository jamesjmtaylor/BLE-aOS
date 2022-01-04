package com.jamesjmtaylor.blecompose.models

//NOTE: For Fitness Machine, FitnessMachineFeatures is the only characteristic that is read (all others are notify)
//NOTE: Only the first 8 bytes of the UUID matter.  All others are masked.
enum class GattCharacteristic(val uuid: String, val notify: Boolean){
    MachineStatus("00002ada-0000-1000-8000-00805f9b34fb", true), //See MachineStatus enum class for all possible states
    IndoorBikeData("00002ad2-0000-1000-8000-00805f9b34fb", true), //See IndoorBikeData enum class for all possible flags
    TrainingStatus("00002ad3-0000-1000-8000-00805f9b34fb", true), //See TraingStatus enum class for all possible states
    TreadmillData("00002acd-0000-1000-8000-00805f9b34fb", true), //See TreadmillData enum class for all possible flags

    FitnessMachineControlPoint("00002ad9-0000-1000-8000-00805f9b34fb", true), //NOT USED: Starts request to control power/speed/incline/resistance/target (heart rate/calories/distance/cadence)
    FitnessMachineFeatures("00002acc-0000-1000-8000-00805f9b34fb", false), // When a bit is set to 1 (True), the Server supports the associated feature. Otherwise set to 0.
    HeartRateMeasurement("00002a37-0000-1000-8000-00805f9b34fb", true), //NOT USED; included as example
    HeartRateSensorLocation("00002a38-0000-1000-8000-00805f9b34fb", false), //NOT USED; included as example

    CscMeasurement("00002a5b-0000-1000-8000-00805f9b34fb",true), //See CyclingSpeedAndCadenceMeasurement enum class for all possible flags
    CscFeature("00002a5c-0000-1000-8000-00805f9b34fb",false), //See CyclingSpeedAndCadenceFeature enum class for all possible flags

    RscMeasurement("00002a53-0000-1000-8000-00805f9b34fb", true), //See RunSpeedAndCadenceMeasurement enum class for all possible flags
    RscFeature("00002a54-0000-1000-8000-00805f9b34fb", true), //See RunSpeedAndCadenceFeature enum class for all possible flags

    SensorLocation("00002a5d-0000-1000-8000-00805f9b34fb",false),
    ScControlPoint("00002a55-0000-1000-8000-00805f9b34fb",true);

    companion object {
        fun getCharacteristic(uuid: String): GattCharacteristic? {
            return values().firstOrNull { it.uuid == uuid }
        }
    }
}