package com.manueldidonna.pk.rby.info

internal fun Int.isEvolutionOf(speciesId: Int): Boolean {
    if (speciesId > this || this > speciesId + 2)
        return false
    return when (speciesId) {
        133 -> this in 134..136 // species id represents an Eevee
        in SingleStages -> this == speciesId + 1
        in FirstStages -> this == speciesId + 1 || this == speciesId + 2
        in SecondStages -> this == speciesId + 1
        else -> false
    }
}

private val SingleStages = intArrayOf(
    19, 21, 23, 25, 27, 35, 37, 39, 41, 46, 48, 50, 52, 54, 56, 58, 72, 77, 79,
    81, 84, 86, 88, 90, 96, 98, 100, 102, 104, 109, 111, 116, 118, 120, 129, 138, 140
)

private val FirstStages = intArrayOf(
    1, 4, 7, 10, 13, 16, 29, 32, 43, 60, 63, 66, 69, 74, 92, 147
)

private val SecondStages = intArrayOf(
    2, 5, 6, 11, 14, 17, 30, 33, 44, 61, 64, 67, 68, 75, 93, 148
)