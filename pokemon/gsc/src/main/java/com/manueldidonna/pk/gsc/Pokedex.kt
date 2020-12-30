package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.utils.getBitAt
import com.manueldidonna.pk.core.Pokedex as CorePokedex

internal class Pokedex(private val data: UByteArray, version: Version) : CorePokedex {
    override val pokemonSpeciesIds: IntRange = 1..251

    private val offsets: Offsets =
        if (version == Version.Crystal) Offsets.InternationalCrystal
        else Offsets.InternationalGoldSilver

    override fun <E> selectEntry(speciesId: Int, mapper: CorePokedex.EntryMapper<E>): E {
        checkEntrySpeciesId(speciesId)
        val bitIndex = getEntryBitIndex(speciesId)
        val entryOffset = getEntryOffset(speciesId)
        return mapper.mapTo(
            speciesId = speciesId,
            isSeen = data[offsets.pokemonSeen + entryOffset].toInt().getBitAt(bitIndex),
            isOwned = data[offsets.pokemonOwned + entryOffset].toInt().getBitAt(bitIndex)
        )
    }

    override fun setEntry(entry: CorePokedex.Entry) {
        checkEntrySpeciesId(entry.speciesId)
        val bitIndex = getEntryBitIndex(entry.speciesId)
        val entryOffset = getEntryOffset(entry.speciesId)
        setFlag(offsets.pokemonSeen + entryOffset, bitIndex, entry.isSeen)
        setFlag(offsets.pokemonOwned + entryOffset, bitIndex, entry.isOwned)
        if (entry.isOwned && entry.speciesId == 201) caughtAllUnown()
    }

    private fun checkEntrySpeciesId(speciesId: Int) {
        require(speciesId in pokemonSpeciesIds) {
            "Species Id $speciesId is out of bounds [$pokemonSpeciesIds]"
        }
    }

    private fun caughtAllUnown() {
        // See all the Unown forms to prevent a crash on pokedex vie
        for (letter in 1..26) data[offsets.unownSeen + letter] = letter.toUByte()
        // First unown letter seen, it determines which sprite to show in the pokedex
        if (data[offsets.firstUnownLetterSeen].toInt() == 0) {
            // 0 isn't valid, change to 'A' by default
            data[offsets.firstUnownLetterSeen] = 1u
        }
    }

    private fun getEntryOffset(speciesId: Int): Int = (speciesId - 1) ushr 3

    private fun getEntryBitIndex(speciesId: Int): Int = (speciesId - 1) and 7

    private fun setFlag(offset: Int, bitIndex: Int, value: Boolean) {
        data[offset] = data[offset] and (1 shl bitIndex).inv().toUByte()
        data[offset] = data[offset] or ((if (value) 1 else 0) shl bitIndex).toUByte()
    }

    private sealed class Offsets {
        abstract val pokemonSeen: Int
        abstract val pokemonOwned: Int
        abstract val unownSeen: Int
        abstract val firstUnownLetterSeen: Int

        object InternationalCrystal : Offsets() {
            override val pokemonSeen = 0x2A47
            override val pokemonOwned = 0x2A27
            override val unownSeen = 0x2A47 + 0x1F
            override val firstUnownLetterSeen = 0x2A47 + 0x1F + 28
        }

        object InternationalGoldSilver : Offsets() {
            override val pokemonSeen = 0x2A6C
            override val pokemonOwned = 0x2A4C
            override val unownSeen = 0x2A6C + 0x1F
            override val firstUnownLetterSeen = 0x2A6C + 0x1F + 28
        }
    }
}
