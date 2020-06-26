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
