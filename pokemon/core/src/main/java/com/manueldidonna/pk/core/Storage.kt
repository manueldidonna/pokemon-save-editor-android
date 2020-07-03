package com.manueldidonna.pk.core

/**
 * A generic interface to store and retrieve pokemon. It's immutable
 */
interface Storage {
    val version: Version // TODO: remove

    val index: Int

    val name: String // TODO: remove

    /**
     * The max amount of [Pokemon] that can be stored in the Inventory
     */
    val capacity: Int

    /**
     * Current number of [Pokemon] stored in the Inventory
     */
    val size: Int

    /**
     * Return an immutable [Pokemon] instance.
     * Should throw an [IllegalStateException] if [slot] isn't lower than [capacity]
     */
    fun getPokemon(slot: Int): Pokemon
}

/**
 * A mutable variant of [Storage]. It will reflects every changes at [Storage.index]
 */
interface MutableStorage : Storage {
    /**
     * Use [MutableStorage.insertPokemon] to notify the Storage about any change
     * in the returned [MutablePokemon]. These changes won't automatically update the Storage.
     *
     * This behavior encourages a strong immutability of the data.
     */
    fun getMutablePokemon(slot: Int): MutablePokemon

    /**
     * Returns true if the pokemon has been inserted.
     */
    fun insertPokemon(pokemon: Pokemon, slot: Int = pokemon.position.slot): Boolean

    fun deletePokemon(slot: Int)
}

/**
 * Insert [pokemon] into the storage.
 * If [MutableStorage.insertPokemon] returns false, delete data from [slot]
 */
fun MutableStorage.insertOrDelete(pokemon: Pokemon, slot: Int) {
    if (!insertPokemon(pokemon, slot)) {
        deletePokemon(slot)
    }
}

/**
 * A read-only collection of [Storage]s
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

    val firstStorage = getMutableStorage(first.index)
    val firstPokemon = firstStorage.getPokemon(first.slot)
    val secondStorage = getMutableStorage(second.index)
    val secondPokemon = secondStorage.getPokemon(second.slot)

    firstStorage.insertOrDelete(secondPokemon, first.slot)
    secondStorage.insertOrDelete(firstPokemon, second.slot)
}
