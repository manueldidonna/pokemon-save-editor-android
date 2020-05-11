package com.manueldidonna.pk.core

interface SaveData {
    val boxCounts: Int

    var currentBoxIndex: Int

    fun getStorage(index: StorageIndex): Storage

    fun getMutableStorage(index: StorageIndex): MutableStorage

    fun exportToBytes(): UByteArray

    interface Factory {
        fun createSaveData(data: UByteArray): SaveData?
    }
}

fun SaveData.deletePokemon(position: Pokemon.Position) {
    getMutableStorage(position.index).deletePokemon(position.slot)
}

fun SaveData.movePokemon(from: Pokemon.Position, to: Pokemon.Position) {
    if (from == to) return // do nothing

    val fromBox = getMutableStorage(from.index)
    val fromData = fromBox.exportPokemonToBytes(from.slot) // not valid
    fromBox.deletePokemon(from.slot)
    getMutableStorage(to.index).importPokemonFromBytes(to.slot, fromData)
}

fun SaveData.swapPokemon(first: Pokemon.Position, second: Pokemon.Position) {
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
