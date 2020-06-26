@file:Suppress("NOTHING_TO_INLINE")

package com.manueldidonna.pk.core

/**
 * A generic interface to store and retrieve pokemon. It's immutable
 */
interface Storage {
    val version: Version // TODO: remove

    val index: StorageIndex

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
 * A mutable variants of [Storage]. It will reflects every changes in [Storage.index]
 */
interface MutableStorage : Storage {
    fun getMutablePokemon(slot: Int): MutablePokemon

    fun importPokemonFromBytes(slot: Int, bytes: UByteArray): Boolean

    fun deletePokemon(slot: Int) // TODO: remove
}

/**
 * A [StorageIndex] is invalid when [StorageIndex.isCurrentBox] == true
 * but [StorageIndex.value] is different than [currentBoxIndex]
 */
inline fun StorageIndex.isInvalid(currentBoxIndex: Int): Boolean {
    return isCurrentBox && value != currentBoxIndex
}

inline val StorageIndex.isParty: Boolean
    get() = value == StorageIndex.Party.value

inline class StorageIndex(private val packedValue: Long) {

    val value: Int
        get() = packedValue.shr(32).toInt()

    val isCurrentBox: Boolean
        get() = !isParty && (packedValue.and(0xFFFFFFFF) == 1L)

    companion object {
        val Party = Box(-1, false)

        @Suppress("FunctionName")
        fun Box(index: Int, isCurrentBox: Boolean): StorageIndex {
            // pack index and isCurrentBox together for use in inline class
            val packedValue = index.toLong() shl 32 or (if (isCurrentBox) 1L else 0L and 0xFFFFFFFF)
            return StorageIndex(packedValue)
        }
    }
}