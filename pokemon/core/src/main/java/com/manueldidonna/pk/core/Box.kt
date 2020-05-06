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

interface WriteableBox : Box {
    fun getPokemonWriter(slot: Int): PokemonWriter
    fun deletePokemon(slot: Int)
    fun importPokemonFromBytes(slot: Int, bytes: UByteArray): Boolean
}

fun WriteableBox.movePokemon(fromSlot: Int, toSlot: Int) {
    if (fromSlot == toSlot) return // Do nothing

    val fromData = exportPokemonToBytes(fromSlot)
    importPokemonFromBytes(toSlot, fromData)
    deletePokemon(fromSlot)
}

fun WriteableBox.swapPokemon(firstSlot: Int, secondSlot: Int) {
    if (firstSlot == secondSlot) return // Do nothing

    val firstData = exportPokemonToBytes(firstSlot)
    val secondData = exportPokemonToBytes(secondSlot)
    if (!importPokemonFromBytes(firstSlot, secondData)) {
        deletePokemon(firstSlot)
    }
    if (!importPokemonFromBytes(secondSlot, firstData)) {
        deletePokemon(secondSlot)
    }
}