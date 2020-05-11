package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.MutableStorage
import com.manueldidonna.pk.core.StorageIndex
import com.manueldidonna.pk.core.isParty

/**
 * Party and Box have the same structure. Box has 20 slots, Party has 6 slots.
 *
 * 0x00 0x1 - pokemon counts (value: 0 - 6/20)
 * 0x01 0x1 - species ID (amount: n. of available slots. Add 0xFF after the latest species id)
 * ---- 0x1 - unused padding (from the latest species id to the pokemon data offset)
 * ----	0x21 - pokemon data (@see [Pokemon]) (ofs: P0x08, B0x16)
 * ---- 0xB - original trainer name (ofs: P0x110, B0x2AA)
 * ---- 0xB - pokemon name (ofs: P0x152, B0x386)
 */
internal class Storage(
    private val data: UByteArray,
    private val startOffset: Int,
    override val index: StorageIndex,
    override val pokemonCounts: Int
) : MutableStorage {

    override val name = if (index.isParty) "PARTY" else "Box ${index.value + 1}"

    override var currentPokemonCounts: Int
        get() = data[startOffset].toInt()
        private set(value) {
            val coercedValue = value.coerceAtMost(pokemonCounts)
            data[startOffset] = (coercedValue).toUByte()
            data[startOffset + coercedValue + 1] = 0xFF.toUByte()
        }

    override fun getPokemon(slot: Int): com.manueldidonna.pk.core.Pokemon {
        require(slot in 0 until pokemonCounts) { "Pokemon slot $slot is out of bounds" }
        return Pokemon.newImmutableInstance(exportPokemonToBytes(slot), index, slot)
    }

    override fun getMutablePokemon(slot: Int): MutablePokemon {
        require(startOffset != 0) { "This box instance is read-only" }
        require(slot in 0 until pokemonCounts) { "Pokemon slot $slot is out of bounds" }
        return Pokemon(
            data = data,
            startOffset = slot.dataOfs,
            trainerNameOffset = slot.trainerNameOfs,
            pokemonNameOffset = slot.nameOfs,
            index = index,
            slot = slot
        )
    }

    override fun exportPokemonToBytes(slot: Int): UByteArray {
        require(slot in 0 until pokemonCounts) { "Pokemon slot $slot is out of bounds" }

        // I treat it as an empty location
        if (slot > currentPokemonCounts - 1)
            return UByteArray(PokemonSize)

        return UByteArray(PokemonSize).apply {
            // Copy Pokemon Box Data
            slot.dataOfs.let { ofs ->
                data.copyInto(this, 0, ofs, ofs + PokemonDataSize)
            }
            // Copy Trainer Name
            slot.trainerNameOfs.let { ofs ->
                data.copyInto(this, PokemonDataSize, ofs, ofs + NameSize)
            }
            // Copy Pokemon Name
            slot.nameOfs.let { ofs ->
                data.copyInto(this, PokemonDataSize + NameSize, ofs, ofs + NameSize)
            }
        }
    }

    override fun importPokemonFromBytes(slot: Int, bytes: UByteArray): Boolean {
        require(startOffset != 0) { "This box instance is read-only" }
        require(slot in 0 until pokemonCounts) { "Pokemon slot $slot is out of bounds" }

        if (bytes.all { it == 0.toUByte() })
            return false // empty pk

        // increase pokemon counts if needed
        if (slot >= currentPokemonCounts)
            currentPokemonCounts++

        val coercedSlot = slot.coerceAtMost(currentPokemonCounts - 1)

        // verify the correctness of the data
        val immutablePokemon = Pokemon.newImmutableInstance(bytes, index, slot)

        data[startOffset + 0x1 + coercedSlot] = immutablePokemon.speciesId.toUByte()
        bytes.apply {
            // Set Pokemon Box Data
            copyInto(data, coercedSlot.dataOfs, 0, PokemonDataSize)
            // Set Trainer Name
            copyInto(data, coercedSlot.trainerNameOfs, PokemonDataSize, PokemonDataSize + NameSize)
            // Set Pokemon Names
            copyInto(data, coercedSlot.nameOfs, PokemonDataSize + NameSize)
        }
        return true
    }

    override fun deletePokemon(slot: Int) {
        require(startOffset != 0) { "This box instance is read-only" }
        require(slot in 0 until pokemonCounts) { "Pokemon slot $slot is out of bounds" }

        if (slot >= currentPokemonCounts)
            return // empty slot

        val endSlot = pokemonCounts - 1

        // Decrease pokemon counts and check if the selected slot is lower than the last empty slot
        // In gen 1 there couldn't be empty slots between the pokemon in the box
        // Move back the pokemon from 1 position if their slot is greater than the selected one
        if (slot < --currentPokemonCounts) {

            /**
             * [end] is the start offset of the last pk to move.
             * The end index is calculated summing [end] to [dataSize]
             */
            fun movePokemonData(destination: Int, start: Int, end: Int, dataSize: Int) {
                data.copyInto(data, destination, start, end + dataSize)
            }

            val startSlot = slot + 1

            movePokemonData(slot.dataOfs, startSlot.dataOfs, endSlot.dataOfs, PokemonDataSize)
            movePokemonData(
                slot.trainerNameOfs,
                startSlot.trainerNameOfs,
                endSlot.trainerNameOfs,
                NameSize
            )
            movePokemonData(slot.nameOfs, startSlot.nameOfs, endSlot.nameOfs, NameSize)
        }

        /**
         * Fill box data with 0x0u to erase its content
         */
        fun erasePokemonData(start: Int, size: Int) {
            data.fill(0u, start, start + size)
        }

        erasePokemonData(endSlot.dataOfs, PokemonDataSize)
        erasePokemonData(endSlot.trainerNameOfs, NameSize)
        erasePokemonData(endSlot.nameOfs, NameSize)
    }

    // don't use this value to export/import pokemon. Use PokemonDataSize instead
    private val pokemonSize = if (index.isParty) PokemonPartyDataSize else PokemonDataSize

    private val trainerNameOffset = startOffset + if (index.isParty) 0x110 else 0x2AA
    private val nameOffset = startOffset + if (index.isParty) 0x152 else 0x386
    private val dataOffset = startOffset + if (index.isParty) 0x8 else 0x16

    private val Int.dataOfs: Int get() = dataOffset + (pokemonSize * this)
    private val Int.trainerNameOfs: Int get() = trainerNameOffset + (NameSize * this)
    private val Int.nameOfs: Int get() = nameOffset + (NameSize * this)
}