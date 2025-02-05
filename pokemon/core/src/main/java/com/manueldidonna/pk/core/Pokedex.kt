package com.manueldidonna.pk.core

/**
 * Store info about seen/owned pokemon.
 */
interface Pokedex {
    /**
     * Ids of the pokemon species contained in this Pokedex.
     *
     * @see Pokedex.pokemonCount
     */
    val pokemonSpeciesIds: IntRange

    /**
     * [speciesId] must be included in [pokemonSpeciesIds]
     */
    fun <E> selectEntry(speciesId: Int, mapper: EntryMapper<E>): E

    fun interface EntryMapper<E> {
        fun mapTo(speciesId: Int, isSeen: Boolean, isOwned: Boolean): E
    }

    fun setEntry(entry: Entry)

    interface Entry {
        val speciesId: Int
        val isSeen: Boolean
        val isOwned: Boolean
    }
}

/**
 * Number of pokemon species contained in this Pokedex-
 */
inline val Pokedex.pokemonCount: Int
    get() = pokemonSpeciesIds.last - pokemonSpeciesIds.first + 1

/**
 * Set [entry] in the pokedex and return a new [E] instance with [mapper]
 */
fun <E : Pokedex.Entry> Pokedex.setEntry(entry: E, mapper: Pokedex.EntryMapper<E>): E {
    setEntry(entry)
    return selectEntry(entry.speciesId, mapper)
}

fun Pokedex.setAllCaught() {
    val reusableEntry = object : Pokedex.Entry {
        override val isSeen: Boolean = true
        override val isOwned: Boolean = true
        override var speciesId: Int = 0
    }
    for (i in pokemonSpeciesIds) {
        reusableEntry.speciesId = i
        setEntry(reusableEntry)
    }
}

fun Pokedex.setAllSeen() {
    val reusableEntry = object : Pokedex.Entry {
        override val isSeen: Boolean = true
        override var isOwned: Boolean = false
        override var speciesId: Int = 0
    }
    val updateEntry = Pokedex.EntryMapper { speciesId, _, isOwned ->
        reusableEntry.speciesId = speciesId
        reusableEntry.isOwned = isOwned
    }
    for (i in pokemonSpeciesIds) {
        selectEntry(speciesId = i, updateEntry)
        setEntry(reusableEntry)
    }
}


/**
 * Set a pokemon as both seen and owned in the pokedex
 */
fun Pokedex.catchPokemonById(speciesId: Int) {
    setEntry(object : Pokedex.Entry {
        override val isOwned = true
        override val isSeen = true
        override val speciesId = speciesId
    })
}
