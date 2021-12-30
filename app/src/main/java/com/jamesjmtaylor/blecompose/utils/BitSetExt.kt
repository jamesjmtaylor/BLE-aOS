package com.jamesjmtaylor.blecompose.utils

typealias BitSet = java.util.BitSet
fun BitSet.toInt(numBits: Int): Int {
    var value = 0
    var isNegative = false
    for (i in 0 until this.length()) {
        if (i == numBits - 1) isNegative = this[numBits - 1] // handle two's compliment
        else value += if (this[i]) 1 shl i else 0
    }
    if (isNegative) return value * -1
    return value
}
