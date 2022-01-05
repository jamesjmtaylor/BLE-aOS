package com.jamesjmtaylor.blecompose.models.characteristics

import com.jamesjmtaylor.blecompose.utils.fromUnsignedBytesToInt
import com.jamesjmtaylor.blecompose.utils.toBitSet
import com.jamesjmtaylor.blecompose.utils.toInt
import timber.log.Timber
import kotlin.math.roundToInt

enum class TreadmillData(val flagBitNumber: Int, val byteSize: Int, val signed: Boolean, val units: String, val resolution: Double){
    ExceedsAttMtuSize(0,0,false, "",0.0),
    InstantaneousSpeedPresent(0,2,false, "kph",0.01),
    AverageSpeedPresent(1,2,false, "kph",0.01),
    TotalDistancePresent(2,2,false, "meters", 1.0),
    InclinePresent(3, 2,true, "%", 0.1),
    RampAnglePresent(3, 2,true, "º", 0.1),
    PositiveElevationGainPresent (4, 2,false,"meters", 0.1),
    NegativeElevationGainPresent (4, 2,false,"meters", 0.1),
    InstantaneousPacePresent(5, 1,false, "km/min",0.1),
    AveragePacePresent (6,1, true, "km/min",0.1),
    TotalEnergyPresent(7,2,false, "calories",1.0),
    EnergyPerHourPresent(7,2,false, "cal/hr",1.0),
    EnergyPerMinutePresent(7,1,false, "cal/min",1.0),
    HeartRatePresent(8,1,false, "bpm",1.0),
    MetabolicEquivalentPresent(9,1,false, "me", 0.1),
    ElapsedTimePresent(10,2,false, "seconds", 1.0),
    RemainingTimePresent(11,2,false,"seconds", 1.0),
    ForceOnBeltPresent(12,2,true,"netwons", 1.0),
    PowerOutputPresent(12,2,true,"watts", 1.0);
    companion object {
        private fun getEnum(bitNumber: Int): TreadmillData {
            return values().first { it.flagBitNumber == bitNumber }
        }
        private fun convertBytesToFlags(bytes: ByteArray): List<TreadmillData> {
            val flags = mutableListOf<TreadmillData>()
            val bitSet = bytes.copyOfRange(0,2).toBitSet()
            if (!bitSet.get(0)) flags.add(InstantaneousSpeedPresent)
            for (i in 0 until bitSet.size()){//TODO: Does not handle if bit 0 is true (Exceeds MTU size)
                Timber.i("Bitset bit $i = ${bitSet.get(i)}")
                if (bitSet.get(i)) flags.add(getEnum(i))
            }
            return flags.toList()
        }
        @ExperimentalUnsignedTypes
        fun convertBytesToDataString(bytes: ByteArray): String {
            val flags = convertBytesToFlags(bytes)
            var currentByteIndex = 2 //First two bytes are used for flags.
            val sb = StringBuilder()
            Timber.i("3rd Party- flags: $flags")
            for (flag in flags){
                Timber.i("3rd Party- byte size: ${flag.byteSize}")
                val dataEndByteIndex = currentByteIndex + flag.byteSize
                val dataBytes = bytes.copyOfRange(currentByteIndex, dataEndByteIndex)
                currentByteIndex = dataEndByteIndex
                val int = if (!flag.signed) dataBytes.fromUnsignedBytesToInt() else dataBytes.toInt()
                val doubleValue = (int * flag.resolution * 10).roundToInt().toDouble() / 10.0 //Removes rounding errors
                sb.append("${flag.name}: $doubleValue ${flag.units} \n")
            }

            return sb.toString()
        }
    }
}