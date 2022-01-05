package com.jamesjmtaylor.blecompose.models.characteristics

import com.jamesjmtaylor.blecompose.utils.fromUnsignedBytesToInt
import com.jamesjmtaylor.blecompose.utils.toBitSet
import com.jamesjmtaylor.blecompose.utils.toInt
import timber.log.Timber
import kotlin.math.roundToInt

enum class IndoorBikeData(val flagBitNumber: Int, val byteSize: Int, val signed: Boolean, val units: String, val resolution: Double){
    ExceedsAttMtuSize(0,0,false, "",0.0),
    InstantaneousSpeedPresent(0,2,false, "kph",0.01),
    AverageSpeedPresent(1,2,false, "kph", 0.01),
    InstantaneousCadence(2, 2,false, "per minute", 0.5),
    AverageCadencePresent (3, 2,false,"per minute", 0.5),
    TotalDistancePresent(4, 3,false, "m",1.0),
    ResistanceLevelPresent (5,2, true, "",1.0),//Unitless
    InstantaneousPowerPresent(6, 2,true, "watts",1.0),
    AveragePowerPresent(7,2,true,"watts",1.0),
    TotalEnergyPresent(8,2,false, "calories",1.0),
    EnergyPerHourPresent(8,2,false, "cal/hr",1.0),
    EnergyPerMinutePresent(8,1,false, "cal/min",1.0),
    HeartRatePresent(9,1,false, "bpm",1.0),
    MetabolicEquivalentPresent(10,1,false, "me", 0.1),
    ElapsedTimePresent(11,2,false, "seconds", 1.0),
    RemainingTimePresent(12,2,false,"seconds", 1.0);
    companion object {
        private fun getFlagsForBitIndex(index: Int): List<IndoorBikeData> {
            return values().filter { it.flagBitNumber == index }
        }
        fun convertBytesToFlags(bytes: ByteArray): List<IndoorBikeData> {
            val flags = mutableListOf<IndoorBikeData>()
            val bitSet = bytes.copyOfRange(0,2).toBitSet()
            if (!bitSet.get(0)) flags.add(InstantaneousSpeedPresent)
            for (i in 0 until bitSet.size()){//TODO: Does not handle if bit 0 is true (Exceeds MTU size)
                Timber.i("Bitset bit $i = ${bitSet.get(i)}")
                if (bitSet.get(i)) flags.addAll(getFlagsForBitIndex(i))
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