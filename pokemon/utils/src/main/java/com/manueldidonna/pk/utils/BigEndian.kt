package com.manueldidonna.pk.utils

fun UByteArray.readBigEndianUShort(offset: Int): UShort {
    var value = 0
    value = value or (this[offset + 0].toInt() shl 8)
    value = value or (this[offset + 1].toInt() shl 0)
    return value.toUShort()
}

fun UByteArray.readBigEndianUInt(offset: Int): UInt {
    var value = 0u
    value = value or (this[offset].toUInt() shl 24)
    value = value or (this[offset + 1].toUInt() shl 16)
    value = value or (this[offset + 2].toUInt() shl 8)
    value = value or this[offset + 3].toUInt()
    return value
}

fun UByteArray.writeBidEndianShort(offset: Int, value: Short) {
    require(size >= offset)
    this[offset] = (value.toInt() ushr 8).toUByte()
    this[offset + 1] = value.toUByte()
}

fun UByteArray.writeBidEndianInt(offset: Int, value: Int, write3Bytes: Boolean = false) {
    require(size >= offset)
    this[offset] = (value ushr 24).toUByte()
    this[offset + 1] = (value ushr 16).toUByte()
    this[offset + 2] = (value ushr 8).toUByte()
    if (!write3Bytes)
        this[offset + 3] = value.toUByte()
}

fun UByteArray.readBigEndianInt(offset: Int): Int {
    var value = 0
    value = value or (this[offset].toInt() shl 24)
    value = value or (this[offset + 1].toInt() shl 16)
    value = value or (this[offset + 2].toInt() shl 8)
    value = value or this[offset + 3].toInt()
    return value
}