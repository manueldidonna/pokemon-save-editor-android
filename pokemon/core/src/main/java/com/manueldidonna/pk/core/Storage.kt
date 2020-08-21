package com.manueldidonna.pk.core

/**
 * A read-only collection of [Pokemon] with a fixed capacity.
 */
interface Storage {
    val name: String // TODO: remove

    /**
     * The maximum number of [Pokemon] that can fit in this storage.
     */
    val capacity: Int

    /**
     * Current number of [Pokemon] in this storage.
     */
    val size: Int

    /**
     * Return a [Pokemon] instance.
     * @see Pokemon.toMutablePokemon
     *
     * Should throw an [IllegalStateException] if [index] isn't lower than [capacity]
     */
    operator fun get(index: Int): Pokemon
}

/**
 * A mutable variant of [Storage].
 */
interface MutableStorage : Storage {
    /**
     * Replaces the pokemon at the specified [index] in this storage with the specified [pokemon].
     *
     * Should throw an [IllegalStateException] if [index] isn't lower than [capacity]
     */
    operator fun set(index: Int, pokemon: Pokemon)

    /**
     * Removes a pokemon at the specified [index] from the storage.
     * Return the pokemon that has been removed.
     *
     * Should throw an [IllegalStateException] if [index] isn't lower than [capacity]
     */
    fun removeAt(index: Int): Pokemon
}

/**
 * A read-only collection of [Storage]
 */
interface StorageCollection {
    /**
     * Indices of the [Storage]s contained in the collection
     */
    val indices: IntRange

    fun getStorage(index: Int): Storage

    fun getMutableStorage(index: Int): MutableStorage

    companion object {
        /**
         * Represent the position of a Party, a particular kind of [Storage]
         *
         * @see [Int.isPartyIndex]
         */
        const val PartyIndex: Int = -1
    }
}

inline val Int.isPartyIndex: Boolean
    get() = this == StorageCollection.PartyIndex

fun StorageCollection.swapPokemon(first: Pokemon.Position, second: Pokemon.Position) {
    if (first == second) return // do nothing

    val firstStorage = getMutableStorage(first.storageIndex)
    val firstPokemon = firstStorage[first.pokemonIndex]
    val secondStorage = getMutableStorage(second.storageIndex)
    val secondPokemon = secondStorage[second.pokemonIndex]

    firstStorage[first.pokemonIndex] = secondPokemon
    secondStorage[second.pokemonIndex] = firstPokemon
}
