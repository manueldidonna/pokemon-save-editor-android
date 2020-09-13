package com.manueldidonna.pk.core

/**
 * A read-only collection of [Pokemon] with a fixed capacity.
 */
interface Storage {
    val name: String

    /**
     * The maximum number of [Pokemon] that can fit in this storage.
     */
    val capacity: Int

    /**
     * Current number of [Pokemon] in this storage.
     */
    val size: Int

    /**
     * A [Pokemon.Factory] to create a valid pokemon for this storage.
     */
    val pokemonFactory: Pokemon.Factory

    /**
     * Return a [Pokemon] instance or null if the [index] is empty.
     *
     * Should throw an [IllegalStateException] if [index] isn't lower than [capacity]
     */
    operator fun get(index: Int): Pokemon?

    /**
     * Return a new [MutableStorage] instance.
     */
    fun toMutableStorage(): MutableStorage

    /**
     * Return a bytes representation of this storage.
     */
    fun exportToBytes(): UByteArray
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
     *
     * Should throw an [IllegalStateException] if [index] isn't lower than [capacity]
     */
    fun removeAt(index: Int)
}

/**
 * Replaces the pokemon at the specified [index] in this storage with the specified [pokemon]
 * if it's not null, otherwise removes a pokemon at [index]
 */
fun MutableStorage.setOrRemove(index: Int, pokemon: Pokemon?) {
    if (pokemon.isEmpty()) removeAt(index) else set(index, pokemon)
}
