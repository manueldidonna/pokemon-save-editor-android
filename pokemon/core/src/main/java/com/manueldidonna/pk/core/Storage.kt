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

    fun exportPokemonToBytes(slot: Int): UByteArray
}

/**
 * A mutable variant of [Storage]. It will reflects every changes at [Storage.index]
 */
interface MutableStorage : Storage {
    fun getMutablePokemon(slot: Int): MutablePokemon

    fun importPokemonFromBytes(slot: Int, bytes: UByteArray): Boolean

    fun deletePokemon(slot: Int) // TODO: remove
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

fun StorageCollection.movePokemon(from: Pokemon.Position, to: Pokemon.Position) {
    if (from == to) return // do nothing

    val fromBox = getMutableStorage(from.index)
    val fromData = fromBox.exportPokemonToBytes(from.slot) // not valid
    fromBox.deletePokemon(from.slot)
    getMutableStorage(to.index).importPokemonFromBytes(to.slot, fromData)
}

fun StorageCollection.swapPokemon(first: Pokemon.Position, second: Pokemon.Position) {
    if (first == second) return // do nothing

    val firstBox = getMutableStorage(first.index)
    val secondBox = getMutableStorage(second.index)
    val firstData = firstBox.exportPokemonToBytes(first.slot)
    val secondData = secondBox.exportPokemonToBytes(second.slot)
    if (!firstBox.importPokemonFromBytes(first.slot, secondData)) {
        firstBox.deletePokemon(first.slot)
    }
    if (!secondBox.importPokemonFromBytes(second.slot, firstData)) {
        secondBox.deletePokemon(second.slot)
    }
}
