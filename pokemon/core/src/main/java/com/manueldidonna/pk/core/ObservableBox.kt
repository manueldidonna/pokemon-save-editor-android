package com.manueldidonna.pk.core

class ObservableBox(private val internalBox: WriteableBox) : WriteableBox by internalBox {
    var onChange: (() -> Unit)? = null

    override fun deletePokemon(slot: Int) {
        internalBox.deletePokemon(slot)
        onChange?.invoke()
    }

    override fun importPokemonFromBytes(slot: Int, bytes: UByteArray): Boolean {
        val result = internalBox.importPokemonFromBytes(slot, bytes)
        if (result)
            onChange?.invoke()
        return result
    }
}