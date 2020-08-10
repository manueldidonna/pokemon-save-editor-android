package com.manueldidonna.pk.resources

import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.core.generation

private val SecondGenLocationIds = 1..94

fun getLocationsIdsBy(version: Version): IntRange {
    return when (version.generation) {
        1 -> IntRange.EMPTY
        2 -> SecondGenLocationIds
        else -> throw IllegalStateException("Unsupported version: $version")
    }
}

fun isLocationIdValid(id: Int, version: Version): Boolean {
    return when (version.generation) {
        1 -> false
        2 -> id in SecondGenLocationIds
        else -> throw IllegalStateException("Unsupported version: $version")
    }
}
