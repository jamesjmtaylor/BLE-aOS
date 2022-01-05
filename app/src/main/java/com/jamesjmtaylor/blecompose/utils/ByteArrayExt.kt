package com.jamesjmtaylor.blecompose.utils

fun ByteArray.toInt(): Int {
    val numBits = this.size * 8
    return BitSet.valueOf(this).toInt(numBits)
}

fun ByteArray.fromUnsignedBytesToInt(): Int {
//Note: UInt is always 32 bits (4 bytes) regardless of platform architecture https://kotlinlang.org/docs/basic-types.html#unsigned-integers
    val bytes = 4
    val paddedArray = ByteArray(bytes)
    for (i in 0 until bytes-this.size) paddedArray[i] = 0
    for (i in bytes-this.size until paddedArray.size) paddedArray[i] = this[i-(bytes-this.size)]

    return (((paddedArray[0].toULong() and 0xFFu) shl 24) or
            ((paddedArray[1].toULong() and 0xFFu) shl 16) or
            ((paddedArray[2].toULong() and 0xFFu) shl 8) or
            (paddedArray[3].toULong() and 0xFFu)).toInt()
}

fun ByteArray.fromUnsignedBytesToLong(): Long {
//Note: UInt is always 32 bits (4 bytes), ULong is always 64 bits (8 bytes), regardless of platform architecture https://kotlinlang.org/docs/basic-types.html#unsigned-integers
    val bytes = 8
    val paddedArray = ByteArray(bytes)
    for (i in 0 until bytes-this.size) paddedArray[i] = 0
    for (i in bytes-this.size until paddedArray.size) paddedArray[i] = this[i-(bytes-this.size)]

    return (((paddedArray[0].toULong() and 0xFFu) shl 56) or
            ((paddedArray[1].toULong() and 0xFFu) shl 48) or
            ((paddedArray[2].toULong() and 0xFFu) shl 40) or
            ((paddedArray[3].toULong() and 0xFFu) shl 32) or
            ((paddedArray[4].toULong() and 0xFFu) shl 24) or
            ((paddedArray[5].toULong() and 0xFFu) shl 16) or
            ((paddedArray[6].toULong() and 0xFFu) shl 8) or
            (paddedArray[7].toULong() and 0xFFu)).toLong()
}

fun ByteArray.toBitSet(): BitSet {
    return BitSet.valueOf(this)
}