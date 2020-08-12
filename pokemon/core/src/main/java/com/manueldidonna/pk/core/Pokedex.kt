package com.manueldidonna.pk.core

/**
 * An interface to get info about seen/owned pokemon
 */
interface Pokedex {
    val pokemonCount: Int

    /**
     * Retrieve info about a Pokedex Entry without instantiate any [Entry] class.
     *
     * To get an immutable [Entry] instance,
     * use Pokedex.selectItem(speciesId, mapTo = Pokedex.Entry::Immutable)
     */
    fun <E> selectEntry(
        speciesId: Int,
        mapTo: (speciesId: Int, isSeen: Boolean, isOwned: Boolean) -> E
    ): E

    fun setEntry(entry: Entry)

    interface Entry {
        val speciesId: Int
        val isSeen: Boolean
        val isOwned: Boolean

        /**
         * Used to represent an immutable [Entry]
         */
        data class Immutable(
            override val speciesId: Int,
            override val isSeen: Boolean,
            override val isOwned: Boolean
        ) : Entry
    }
}
