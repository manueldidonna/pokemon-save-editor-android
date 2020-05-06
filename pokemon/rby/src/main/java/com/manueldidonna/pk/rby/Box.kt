package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.WriteableBox

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
) : WriteableBox {

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

    override fun getPokemon(slot: Int): Pokemon {
        require(slot in 0..20) { "Pokemon position must be 0-20" }

        return Pokemon(data = exportPokemonToBytes(slot), box = index, slot = slot)
    }

    override fun exportPokemonToBytes(slot: Int): UByteArray {
        require(slot in 0..20) { "Pokemon slot must be 0-20" }

        // I treat it as an empty location
        if (slot > currentPokemonCounts - 1)
            return UByteArray(FULL_PK_SIZE)

        return UByteArray(FULL_PK_SIZE).apply {
            // Copy Pokemon Box Data
            slot.dataOfs.let { ofs ->
                data.copyInto(this, 0, ofs, ofs + PK_DATA_SIZE)
            }
            // Copy Trainer Name
            slot.trainerNameOfs.let { ofs ->
                data.copyInto(this, PK_DATA_SIZE, ofs, ofs + NAME_SIZE)
            }
            // Copy Pokemon Name
            slot.nameOfs.let { ofs ->
                data.copyInto(this, PK_DATA_SIZE + NAME_SIZE, ofs, ofs + NAME_SIZE)
            }
        }
    }

    override fun getPokemonWriter(slot: Int): PokemonWriter {
        require(startOffset != 0) { "This box instance is read-only" }
        require(slot in 0..20) { "Pokemon position must be 0-20" }

        return PokemonWriter(data, slot.speciesOfs, slot.dataOfs, slot.trainerNameOfs, slot.nameOfs)
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

        data[coercedSlot.speciesOfs] = Pokemon(bytes, index, slot).speciesId.toUByte()
        bytes.apply {
            // Set Pokemon Box Data
            copyInto(data, coercedSlot.dataOfs, 0, PK_DATA_SIZE)
            // Set Trainer Name
            copyInto(data, coercedSlot.trainerNameOfs, PK_DATA_SIZE, PK_DATA_SIZE + NAME_SIZE)
            // Set Pokemon Names
            copyInto(data, coercedSlot.nameOfs, PK_DATA_SIZE + NAME_SIZE)
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

            movePokemonData(slot.dataOfs, startSlot.dataOfs, 19.dataOfs, PK_DATA_SIZE)
            movePokemonData(
                slot.trainerNameOfs,
                startSlot.trainerNameOfs,
                19.trainerNameOfs,
                NAME_SIZE
            )
            movePokemonData(slot.nameOfs, startSlot.nameOfs, 19.nameOfs, NAME_SIZE)
        }

        /**
         * Fill box data with 0x0u to erase its content
         */
        fun erasePokemonData(start: Int, size: Int) {
            data.fill(0u, start, start + size)
        }

        erasePokemonData(19.dataOfs, PK_DATA_SIZE)
        erasePokemonData(19.trainerNameOfs, NAME_SIZE)
        erasePokemonData(19.nameOfs, NAME_SIZE)
    }

    private val Int.speciesOfs: Int get() = startOffset + 0x1 + this
    private val Int.dataOfs: Int get() = startOffset + 0x16 + (PK_DATA_SIZE * this)
    private val Int.trainerNameOfs: Int get() = startOffset + TRAINER_NAME_OFFSET + (NAME_SIZE * this)
    private val Int.nameOfs: Int get() = startOffset + PK_NAME_OFFSET + (NAME_SIZE * this)

    companion object {
        private const val FULL_PK_SIZE = 0x37
        private const val PK_DATA_SIZE = 0x21
        private const val NAME_SIZE = 0xb
        private const val PK_NAME_OFFSET = 0x386
        private const val TRAINER_NAME_OFFSET = 0x2AA
    }
}