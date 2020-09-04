package com.manueldidonna.pk.utils

@Suppress("NOTHING_TO_INLINE")
inline fun UByteArray.copyIntoFor(
    destination: UByteArray,
    destinationOffset: Int,
    startIndex: Int,
    length: Int,
) = copyInto(destination, destinationOffset, startIndex, startIndex + length)
