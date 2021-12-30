package com.jamesjmtaylor.blecompose.models.characteristics

enum class TrainingStatus(val hexValue: Byte){
    Other(0x00),
    Idle(0x01),
    WarmingUp(0x02),
    LowIntensityInterval(0x03),
    HighIntensityInterval(0x04),
    RecoveryInterval(0x05),
    Isometric(0x06),
    HeartRateControl(0x07),
    FitnessTest(0x08),
    SpeedOutsideControlRegionLow(0x09),
    SpeedOutsideControlRegionHigh (0x0A),
    CoolDown(0x0B),
    WattControl(0x0C),
    ManualMode (0x0D),
    PreWorkout(0x0E),
    PostWorkout(0x0F);

    companion object {
        fun getEnum(hexValue: Byte): TrainingStatus {
            return values().first { it.hexValue == hexValue }
        }
    }
}