package com.manueldidonna.pk.gsc.converter

import com.manueldidonna.pk.core.Pokemon as CorePokemon

internal fun getUnownLetterFromStatistics(iV: CorePokemon.StatisticValues): Char {
    val letterIndex = with(iV) {
        var letterIndex = attack and 0x6 shl 5
        letterIndex = letterIndex or (defense and 0x6 shl 3)
        letterIndex = letterIndex or (speed and 0x6 shl 1)
        letterIndex = letterIndex or (specialAttack and 0x6 shr 1)
        letterIndex / 10
    }
    require(letterIndex in 0..25) { "Unexpected Unown letter index $letterIndex" }
    return (letterIndex + 'A'.toInt()).toChar()
}
