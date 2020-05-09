package com.manueldidonna.pk.core

interface SaveData {
    val boxCounts: Int

    var currentBoxIndex: Int

    fun getBox(index: Int): Box

    fun getMutableBox(index: Int): MutableBox

    fun exportToBytes(): UByteArray

    interface Factory {
        fun createSaveData(data: UByteArray): SaveData?
    }
}

/**
 * If [value] is greater than [SaveData.boxCounts] go back to the first box
 * If [value] is lower than zero, go to the last box
 *
 * @return the new current box index
 */
fun SaveData.setCoercedBoxIndex(value: Int): Int {
    currentBoxIndex = if (value >= boxCounts) 0 else if (value < 0) boxCounts - 1 else value
    return currentBoxIndex
}

fun SaveData.deletePokemon(position: Pokemon.Position) {
    getMutableBox(position.box).deletePokemon(position.slot)
}

fun SaveData.movePokemon(from: Pokemon.Position, to: Pokemon.Position) {
    if (from == to) return // do nothing

    val fromBox = getMutableBox(from.box)
    val fromData = fromBox.exportPokemonToBytes(from.slot) // not valid
    fromBox.deletePokemon(from.slot)
    getMutableBox(to.box).importPokemonFromBytes(to.slot, fromData)
}

fun SaveData.swapPokemon(first: Pokemon.Position, second: Pokemon.Position) {
    if (first == second) return // do nothing

    val firstBox = getMutableBox(first.box)
    val secondBox = getMutableBox(second.box)
    val firstData = firstBox.exportPokemonToBytes(first.slot)
    val secondData = secondBox.exportPokemonToBytes(second.slot)
    if (!firstBox.importPokemonFromBytes(first.slot, secondData)) {
        firstBox.deletePokemon(first.slot)
    }
    if (!secondBox.importPokemonFromBytes(second.slot, firstData)) {
        secondBox.deletePokemon(second.slot)
    }
}
