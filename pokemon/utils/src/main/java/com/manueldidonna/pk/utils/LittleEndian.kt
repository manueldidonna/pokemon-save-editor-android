package com.manueldidonna.pk.utils

fun UByteArray.setLittleEndianShort(offset: Int, value: Short) {
    require(size >= offset)
    this[offset + 1] = (value.toInt() ushr 8).toUByte()
    this[offset] = value.toUByte()
}
