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
    ) {
        companion object {
            fun neverSeen(speciesId: Int) = Entry(speciesId, isSeen = false, isOwned = false)
            fun onlySeen(speciesId: Int): Entry = Entry(speciesId, isSeen = true, isOwned = false)
            fun owned(speciesId: Int): Entry = Entry(speciesId, isSeen = true, isOwned = true)
        }
    }
}

fun Pokedex.getAllEntries(): List<Pokedex.Entry> {
    return List(pokemonCounts) { getEntry(it + 1) }
}
