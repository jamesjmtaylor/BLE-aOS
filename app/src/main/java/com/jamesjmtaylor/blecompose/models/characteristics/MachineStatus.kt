package com.jamesjmtaylor.blecompose.models.characteristics

enum class MachineStatus(val hexValue: Byte) {
    ReservedForFutureUse(0x0),
    Reset(0x01),
    FitnessMachineStoppedOrPausedByUser(0x02),
    FitnessMachineStoppedSafetyKey(0x03),
    FitnessMachineStartedOrResumedByUser(0x04),
    TargetSpeedChanged(0x05),
    TargetInclineChanged(0x06),
    TargetResistanceLevelChanged(0x07),
    TargetPowerChanged(0x08),
    TargetHeartRateChanged(0x09),
    TargetedExpendedEnergyChanged(0x0A),
    TargetedNumberStepsChanged(0x0B),
    TargetedNumberStridesChanged(0x0C),
    TargetedDistanceChanged(0x0D),
    TargetedTrainingTimeChanged(0x0E),
    TargetedChangedTimeInTwoHeartRateZones(0x0F),
    TargetedChangedTimeInThreeHeartRateZones(0x10),
    TargetedChangedTimeInFiveHeartRateZones(0x11),
    IndoorBikeSimulationParametersChanged(0x12),
    WheelCircumferenceChanged(0x13),
    SpinDownStatusFITNESS(0x14),
    TargetedCadenceChanged(0x15);

    companion object {
        fun getEnum(hexValue: Byte): MachineStatus {
            return values().first { it.hexValue == hexValue }
        }
    }
}