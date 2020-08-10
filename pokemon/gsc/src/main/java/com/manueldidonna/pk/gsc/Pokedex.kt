package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.utils.getBitAt
import com.manueldidonna.pk.core.Pokedex as CorePokedex

internal class Pokedex(private val data: UByteArray, version: Version) : CorePokedex {

    override val pokemonCount: Int = 251

    private val seenOffset = if (version == Version.Crystal) 0x2A47 else 0x2A6C
    private val ownedOffset = if (version == Version.Crystal) 0x2A27 else 0x2A4C

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
            data[seenOffset + offset].toInt().getBitAt(bitIndex),
            data[ownedOffset + offset].toInt().getBitAt(bitIndex)
        )
    }

    override fun setEntry(entry: CorePokedex.Entry) {
        require(entry.speciesId in 1..pokemonCount) {
            "SpeciesId ${entry.speciesId} is out of bounds [1 - $pokemonCount]"
        }
        val bitIndex = getEntryBitIndex(entry.speciesId)
        val offset = getEntryOffset(entry.speciesId)
        setFlag(seenOffset + offset, bitIndex, entry.isSeen)
        setFlag(ownedOffset + offset, bitIndex, entry.isOwned)
        if (entry.isOwned && entry.speciesId == 201) {
            caughtAllUnown()
        }
    }

    /**
     * Caught all the Unown forms to prevent a crash on pokedex view
     */
    private fun caughtAllUnown() {
        for (i in 1..26) {
            data[seenOffset + 0x1F + i] = i.toUByte()
        }
        // First unown letter seen, it determines which sprite to show in the pokedex
        if (data[seenOffset + 0x1F + 28].toInt() == 0) {
            // 0 isn't valid, change to 'A' by default
            data[seenOffset + 0x1F + 28] = 1u
        }
    }

    private fun getEntryOffset(speciesId: Int): Int = (speciesId - 1) ushr 3

    private fun getEntryBitIndex(speciesId: Int): Int = (speciesId - 1) and 7

    private fun setFlag(offset: Int, bitIndex: Int, value: Boolean) {
        data[offset] = data[offset] and (1 shl bitIndex).inv().toUByte()
        data[offset] = data[offset] or ((if (value) 1 else 0) shl bitIndex).toUByte()
    }
}
