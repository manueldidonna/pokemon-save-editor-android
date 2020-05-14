package com.manueldidonna.pk.core

/**
 * An interface to get info about seen/owned pokemon
 */
interface Pokedex {
    val pokemonCounts: Int

    fun getEntry(speciesId: Int): Entry

    fun setEntry(entry: Entry)

    data class Entry(
        val speciesId: Int,
        val isSeen: Boolean,
        val isOwned: Boolean
    )
}