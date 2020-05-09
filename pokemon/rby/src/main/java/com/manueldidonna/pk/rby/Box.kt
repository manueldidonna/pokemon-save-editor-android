package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.MutableBox
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokemon as CorePokemon

/**
 * 0x00 0x1 - pokemon counts (value: 0 - 19)
 * 0x01 0x1 - species ID (amount: 20. 0x01 - 0x15. Add 0xFF after the latest species id)
 * 0x15	0x1 - unused padding
 * 0x16	0x21 - pokemon data (amount: 20. @see [Pokemon])
 * 0x2AA 0xB - original trainer name (amount: 20)
 * 0x386 0xB - pokemon name (amount: 20. If it's different from the species name, it's a surname)
 */
internal class Box(
    private val data: UByteArray,
    private val startOffset: Int,
    override val index: Int
) : MutableBox {

    // TODO: translate 'Box'
    override val name: String = "Box ${index + 1}"

    override val pokemonCounts: Int = 20

    override var currentPokemonCounts: Int
        get() = data[startOffset].toInt()
        private set(value) {
            val coercedValue = value.coerceAtMost(pokemonCounts)
            data[startOffset] = (coercedValue).toUByte()
            data[startOffset + coercedValue + 1] = 0xFF.toUByte()
        }

    override fun getPokemon(slot: Int): CorePokemon {
        require(slot in 0..20) { "Pokemon position must be 0-20" }
        return Pokemon.newImmutableInstance(exportPokemonToBytes(slot), index, slot)
    }

    override fun getMutablePokemon(slot: Int): MutablePokemon {
        require(startOffset != 0) { "This box instance is read-only" }
        require(slot in 0..20) { "Pokemon position must be 0-20" }
        return Pokemon(
            data = data,
            startOffset = slot.dataOfs,
            trainerNameOffset = slot.trainerNameOfs,
            pokemonNameOffset = slot.nameOfs,
            box = index,
            slot = slot
        )
    }

    override fun exportPokemonToBytes(slot: Int): UByteArray {
        require(slot in 0..20) { "Pokemon slot must be 0-20" }

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
        require(slot in 0..20) { "Pokemon slot must be 0-20" }

        if (bytes.all { it == 0.toUByte() })
            return false // empty pk

        // increase pokemon counts if needed
        if (slot >= currentPokemonCounts)
            currentPokemonCounts++

        val coercedSlot = slot.coerceAtMost(currentPokemonCounts - 1)

        // verify the correctness of the data
        val immutablePokemon = Pokemon.newImmutableInstance(bytes, index, slot)

        data[coercedSlot.speciesOfs] = immutablePokemon.speciesId.toUByte()
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
        require(slot in 0..20) { "Pokemon slot must be 0-20" }

        if (slot >= currentPokemonCounts)
            return // empty slot

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

            movePokemonData(slot.dataOfs, startSlot.dataOfs, 19.dataOfs, PokemonDataSize)
            movePokemonData(
                slot.trainerNameOfs,
                startSlot.trainerNameOfs,
                19.trainerNameOfs,
                NameSize
            )
            movePokemonData(slot.nameOfs, startSlot.nameOfs, 19.nameOfs, NameSize)
        }

        /**
         * Fill box data with 0x0u to erase its content
         */
        fun erasePokemonData(start: Int, size: Int) {
            data.fill(0u, start, start + size)
        }

        erasePokemonData(19.dataOfs, PokemonDataSize)
        erasePokemonData(19.trainerNameOfs, NameSize)
        erasePokemonData(19.nameOfs, NameSize)
    }

    private val Int.speciesOfs: Int get() = startOffset + 0x1 + this
    private val Int.dataOfs: Int get() = startOffset + 0x16 + (PokemonDataSize * this)
    private val Int.trainerNameOfs: Int get() = startOffset + 0x2AA + (NameSize * this)
    private val Int.nameOfs: Int get() = startOffset + 0x386 + (NameSize * this)
}