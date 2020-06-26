package com.manueldidonna.pk.core

/**
 * A read-only collection of [Storage]s that also save the current index
 */
interface StorageCollection {
    val indices: IntRange

    var currentIndex: Int

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

fun StorageCollection.increaseIndex(): Int {
    val index = currentIndex + 1
    currentIndex = if (index > indices.last) indices.first else index
    return currentIndex
}

fun StorageCollection.decreaseIndex(): Int {
    val index = currentIndex - 1
    currentIndex = if (index < indices.first) indices.last else index
    return currentIndex
}

fun StorageCollection.deletePokemon(position: Pokemon.Position) {
    getMutableStorage(position.index).deletePokemon(position.slot)
}

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
