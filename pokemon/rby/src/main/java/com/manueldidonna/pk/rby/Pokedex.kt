package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.Pokedex as CorePokedex

/**
 * Pokemon are indexed by their usual Pokedex order,
 * meaning the order is the same as in the National Pokedex.
 * However, indexes begin counting at 0, rather than 1
 */
internal class Pokedex(private val data: UByteArray) : CorePokedex {

    companion object {
        private const val OwnedOffset = 0x25A3
        private const val SeenOffset = 0x25B6
    }

    override val pokemonCounts: Int = 151

    override fun getEntry(speciesId: Int): CorePokedex.Entry {
        require(speciesId in 1..151) { "Species Id not supported: $speciesId" }
        val bitIndex = (speciesId - 1) and 7
        val offset = (speciesId - 1) ushr 3
        return CorePokedex.Entry.Immutable(
            speciesId = speciesId,
            isSeen = (data[SeenOffset + offset].toInt() ushr bitIndex and 1) != 0,
            isOwned = (data[OwnedOffset + offset].toInt() ushr bitIndex and 1) != 0
        )
    }

    override fun setEntry(entry: CorePokedex.Entry) {
        require(entry.speciesId in 1..151) { "Species Id not supported: ${entry.speciesId}" }
        val bitIndex = (entry.speciesId - 1) and 7
        val offset = (entry.speciesId - 1) ushr 3
        setFlag(SeenOffset + offset, bitIndex, entry.isSeen)
        setFlag(OwnedOffset + offset, bitIndex, entry.isOwned)
    }

    private fun setFlag(offset: Int, bitIndex: Int, value: Boolean) {
        data[offset] = data[offset] and (1 shl bitIndex).inv().toUByte()
        data[offset] = data[offset] or ((if (value) 1 else 0) shl bitIndex).toUByte()
    }
}
