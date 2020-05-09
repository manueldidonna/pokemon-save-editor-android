package com.manueldidonna.pk.core

interface Box {
    /**
     * Max allowed number of pokemon that can be stored in the box
     */
    val pokemonCounts: Int

    val index: Int

    val name: String

    val currentPokemonCounts: Int

    fun getPokemon(slot: Int): Pokemon

    fun exportPokemonToBytes(slot: Int): UByteArray
}

interface MutableBox : Box {
    fun getMutablePokemon(slot: Int): MutablePokemon

    fun deletePokemon(slot: Int)

    fun importPokemonFromBytes(slot: Int, bytes: UByteArray): Boolean
}