package com.manueldidonna.pk.rby

import com.manueldidonna.pk.utils.getBitAt
import com.manueldidonna.pk.core.Pokedex as CorePokedex

/**
 * Pokemon are indexed by their usual Pokedex order,
 * meaning the order is the same as in the National Pokedex.
 * However, indexes begin counting at 0, rather than 1
 */
internal class Pokedex(private val data: UByteArray) : CorePokedex {
    override val pokemonSpeciesIds = 1..151

    override fun <E> selectEntry(speciesId: Int, mapper: CorePokedex.EntryMapper<E>): E {
        checkEntrySpeciesId(speciesId)
        val bitIndex = getEntryBitIndex(speciesId)
        val entryOffset = getEntryOffset(speciesId)
        return mapper.mapTo(
            speciesId = speciesId,
            isSeen = data[SeenOffset + entryOffset].toInt().getBitAt(bitIndex),
            isOwned = data[OwnedOffset + entryOffset].toInt().getBitAt(bitIndex)
        )
    }

    override fun setEntry(entry: CorePokedex.Entry) {
        checkEntrySpeciesId(entry.speciesId)
        val bitIndex = getEntryBitIndex(entry.speciesId)
        val offset = getEntryOffset(entry.speciesId)
        setFlag(SeenOffset + offset, bitIndex, entry.isSeen)
        setFlag(OwnedOffset + offset, bitIndex, entry.isOwned)
    }

    private fun checkEntrySpeciesId(speciesId: Int) {
        require(speciesId in pokemonSpeciesIds) {
            "Species Id $speciesId is out of bounds [$pokemonSpeciesIds]"
        }
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
