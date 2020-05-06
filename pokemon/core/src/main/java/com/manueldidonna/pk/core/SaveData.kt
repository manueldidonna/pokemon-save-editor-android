package com.manueldidonna.pk.core

interface SaveData {
    val boxCounts: Int
    var currentBoxIndex: Int

    fun getBox(index: Int): Box
    fun getWriteableBox(index: Int): WriteableBox

    fun exportToBytes(): UByteArray

    interface Factory {
        fun createSaveData(data: UByteArray): SaveData?
    }
}

fun SaveData.getCurrentBox(): Box = getBox(currentBoxIndex)

fun SaveData.deletePokemon(position: Pokemon.Position) {
    getWriteableBox(position.box).deletePokemon(position.slot)
}

fun SaveData.movePokemon(from: Pokemon.Position, to: Pokemon.Position) {
    if (from == to) return // do nothing
    if (from.box == to.box) {
        getWriteableBox(from.box).movePokemon(from.slot, to.slot)
    } else {
        val fromBox = getWriteableBox(from.box)
        val fromData = fromBox.exportPokemonToBytes(from.slot) // not valid
        fromBox.deletePokemon(from.slot)
        getWriteableBox(to.box).importPokemonFromBytes(to.slot, fromData)
    }
}

fun SaveData.swapPokemon(first: Pokemon.Position, second: Pokemon.Position) {
    if (first == second) return // do nothing
    if (first.box == second.box) {
        getWriteableBox(first.box).swapPokemon(first.slot, second.slot)
    } else {
        val firstBox = getWriteableBox(first.box)
        val secondBox = getWriteableBox(second.box)
        val firstData = firstBox.exportPokemonToBytes(first.slot)
        val secondData = secondBox.exportPokemonToBytes(second.slot)
        if (!firstBox.importPokemonFromBytes(first.slot, secondData)) {
            firstBox.deletePokemon(first.slot)
        }
        if (!secondBox.importPokemonFromBytes(second.slot, firstData)) {
            secondBox.deletePokemon(second.slot)
        }
    }
}
