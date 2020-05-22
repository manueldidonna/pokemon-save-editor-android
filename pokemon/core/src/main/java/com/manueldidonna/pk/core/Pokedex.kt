package com.manueldidonna.pk.core

/**
 * An interface to get info about seen/owned pokemon
 */
interface Pokedex {
    val pokemonCounts: Int

    fun getEntry(speciesId: Int): Entry

    fun setEntry(entry: Entry)

    interface Entry {
        val speciesId: Int
        val isSeen: Boolean
        val isOwned: Boolean

        data class Immutable(
            override val speciesId: Int,
            override val isSeen: Boolean,
            override val isOwned: Boolean
        ) : Entry

        companion object {
            fun neverSeen(speciesId: Int): Entry {
                return Immutable(speciesId, isSeen = false, isOwned = false)
            }

            fun onlySeen(speciesId: Int): Entry {
                return Immutable(speciesId, isSeen = true, isOwned = false)
            }

            fun owned(speciesId: Int): Entry {
                return Immutable(speciesId, isSeen = true, isOwned = true)
            }
        }
    }
}

fun Pokedex.getAllEntries(): List<Pokedex.Entry> {
    return List(pokemonCounts) { getEntry(it + 1) }
}
