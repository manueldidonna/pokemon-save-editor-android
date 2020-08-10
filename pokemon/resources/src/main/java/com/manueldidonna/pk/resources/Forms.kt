package com.manueldidonna.pk.resources

import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.core.generation

private val UnownLetters = ('A'..'Z').toList() + '!'

fun unownLetters(version: Version): List<Char> {
    return when (version.generation) {
        1 -> throw IllegalStateException("Unown doesn't exists in First gen.")
        2 -> UnownLetters
        else -> UnownLetters + '?'
    }
}

fun isValidUnownLetter(letter: Char, version: Version): Boolean {
    if (letter.toUpperCase() in 'A'..'Z') return true
    if (letter == '!' || (letter == '?' && version.generation >= 3)) return true
    return false
}
