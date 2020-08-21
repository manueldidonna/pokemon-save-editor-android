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
     * Return a [Pokemon] instance.
     *
     * Should throw an [IllegalStateException] if [index] isn't lower than [capacity]
     */
    operator fun get(index: Int): Pokemon

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
     * Return the pokemon that has been removed.
     *
     * Should throw an [IllegalStateException] if [index] isn't lower than [capacity]
     */
    fun removeAt(index: Int): Pokemon
}
