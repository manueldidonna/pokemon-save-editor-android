package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.utils.getBitAt
import com.manueldidonna.pk.core.Pokedex as CorePokedex

internal class Pokedex(private val data: UByteArray, version: Version) : CorePokedex {

    override val pokemonCount: Int = 251

    private val seenOffset: Int
    private val ownedOffset: Int

    init {
        if (version == Version.Crystal) {
            seenOffset = CrystalSeenOffset
            ownedOffset = CrystalOwnedOffset
        } else {
            seenOffset = GoldSilverSeenOffset
            ownedOffset = GoldSilverOwnedOffset
        }
    }

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

    override fun setEntry(entry: com.manueldidonna.pk.core.Pokedex.Entry) {
        require(entry.speciesId in 1..pokemonCount) {
            "SpeciesId ${entry.speciesId} is out of bounds [1 - $pokemonCount]"
        }
        val bitIndex = getEntryBitIndex(entry.speciesId)
        val offset = getEntryOffset(entry.speciesId)
        setFlag(seenOffset + offset, bitIndex, entry.isSeen)
        setFlag(ownedOffset + offset, bitIndex, entry.isOwned)
    }

    private fun getEntryOffset(speciesId: Int): Int = (speciesId - 1) ushr 3

    private fun getEntryBitIndex(speciesId: Int): Int = (speciesId - 1) and 7

    private fun setFlag(offset: Int, bitIndex: Int, value: Boolean) {
        data[offset] = data[offset] and (1 shl bitIndex).inv().toUByte()
        data[offset] = data[offset] or ((if (value) 1 else 0) shl bitIndex).toUByte()
    }

    companion object {
        private const val GoldSilverOwnedOffset = 0x2A4C
        private const val GoldSilverSeenOffset = 0x2A6C
        private const val CrystalOwnedOffset = 0x2A27
        private const val CrystalSeenOffset = 0x2A47
    }
}