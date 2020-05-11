package com.manueldidonna.pk.core

/**
 * A generic interface to store and retrieve pokemon. It's immutable
 */
interface Storage {
    val index: StorageIndex
    val name: String
    val pokemonCounts: Int
    val currentPokemonCounts: Int
    fun getPokemon(slot: Int): Pokemon
    fun exportPokemonToBytes(slot: Int): UByteArray
}

/**
 * A mutable variants of [Storage]. It will reflects every changes in [Storage.index]
 */
interface MutableStorage : Storage {
    fun getMutablePokemon(slot: Int): MutablePokemon
    fun deletePokemon(slot: Int)
    fun importPokemonFromBytes(slot: Int, bytes: UByteArray): Boolean
}

inline val StorageIndex.isParty: Boolean get() = value == StorageIndex.Party.value

inline class StorageIndex(val value: Int) {

    inline fun nextIndex(boxCounts: Int): StorageIndex {
        return when (value) {
            Party.value -> StorageIndex(0)
            in 0 until boxCounts - 1 -> StorageIndex(value + 1)
            else -> Party
        }
    }

    inline fun previousIndex(boxCounts: Int): StorageIndex {
        return when (value) {
            Party.value -> StorageIndex(boxCounts - 1)
            in 1 until boxCounts -> StorageIndex(value - 1)
            else -> Party
        }
    }

    companion object {
        val Party = StorageIndex(-1)
    }
}