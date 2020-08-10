package com.manueldidonna.pk.rby

import com.manueldidonna.pk.utils.getBitAt
import com.manueldidonna.pk.core.Pokedex as CorePokedex

/**
 * Pokemon are indexed by their usual Pokedex order,
 * meaning the order is the same as in the National Pokedex.
 * However, indexes begin counting at 0, rather than 1
 */
internal class Pokedex(private val data: UByteArray) : CorePokedex {

    override val pokemonCount: Int = 151

    override fun <E> selectEntry(
        speciesId: Int,
        mapTo: (speciesId: Int, isSeen: Boolean, isOwned: Boolean) -> E
    ): E {
        require(speciesId in 1..pokemonCount) {
            "SpeciesId $speciesId is out of bounds [1 - $pokemonCount]"
        }
        val bitIndex = getEntryBitIndex(speciesId)
        val offset = getEntryOffset(speciesId)
        return mapTo(
            speciesId,
            data[SeenOffset + offset].toInt().getBitAt(bitIndex),
            data[OwnedOffset + offset].toInt().getBitAt(bitIndex)
        )
    }

    override fun setEntry(entry: CorePokedex.Entry) {
        require(entry.speciesId in 1..pokemonCount) {
            "SpeciesId ${entry.speciesId} is out of bounds [1 - $pokemonCount]"
        }
        val bitIndex = getEntryBitIndex(entry.speciesId)
        val offset = getEntryOffset(entry.speciesId)
        setFlag(SeenOffset + offset, bitIndex, entry.isSeen)
        setFlag(OwnedOffset + offset, bitIndex, entry.isOwned)
    }

    private fun getEntryOffset(speciesId: Int): Int = (speciesId - 1) ushr 3

    private fun getEntryBitIndex(speciesId: Int): Int = (speciesId - 1) and 7

    private fun setFlag(offset: Int, bitIndex: Int, value: Boolean) {
        data[offset] = data[offset] and (1 shl bitIndex).inv().toUByte()
        data[offset] = data[offset] or ((if (value) 1 else 0) shl bitIndex).toUByte()
    }

    companion object {
        private const val OwnedOffset = 0x25A3
        private const val SeenOffset = 0x25B6
    }
}
