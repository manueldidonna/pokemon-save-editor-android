package com.manueldidonna.pk.core

/**
 * Store info about seen/owned pokemon.
 */
interface Pokedex {
    val pokemonCount: Int

    /**
     * Should throw an [IllegalStateException] if [speciesId] is greater than [pokemonCount]
     */
    fun <E> selectEntry(
        speciesId: Int,
        mapTo: (speciesId: Int, isSeen: Boolean, isOwned: Boolean) -> E,
    ): E

    fun setEntry(entry: Entry)

    interface Entry {
        val speciesId: Int
        val isSeen: Boolean
        val isOwned: Boolean

        data class Immutable(
            override val speciesId: Int,
            override val isSeen: Boolean,
            override val isOwned: Boolean,
        ) : Entry
    }
}

fun Pokedex.catchPokemonById(speciesId: Int) {
    setEntry(Pokedex.Entry.Immutable(speciesId, isSeen = true, isOwned = true))
}
