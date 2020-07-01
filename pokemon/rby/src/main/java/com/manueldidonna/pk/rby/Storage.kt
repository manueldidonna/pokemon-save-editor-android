package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.core.Pokemon as CorePokemon

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
    override val index: Int,
    override val capacity: Int,
    override val version: Version
) : MutableStorage {

    override val name = if (index.isPartyIndex) "PARTY" else "Box ${index + 1}"

    override var size: Int
        get() = data[startOffset].toInt()
        private set(value) {
            val coercedValue = value.coerceIn(0, capacity)
            data[startOffset] = (coercedValue).toUByte()
            data[startOffset + coercedValue + 1] = 0xFF.toUByte()
        }

    override fun getPokemon(slot: Int): CorePokemon {
        require(slot in 0 until capacity) { "Pokemon slot $slot is out of bounds [0 - $capacity]" }
        return Pokemon.newImmutableInstance(data, index, slot, version, startOffset)
    }

    override fun getMutablePokemon(slot: Int): MutablePokemon {
        require(startOffset != 0) { "This box instance is read-only" }
        require(slot in 0 until capacity) { "Pokemon slot $slot is out of bounds" }
        val sanitizedSlot = sanitizePokemonSlot(slot)
        val pokemon = Pokemon.newMutableInstance(data, index, sanitizedSlot, version, startOffset)
        if (pokemon.isEmpty) Pokemon.EmptyTemplate.apply(pokemon)
        return pokemon
    }

    override fun insertPokemon(slot: Int, pokemon: CorePokemon): Boolean {
        require(pokemon.version.isFirstGeneration) {
            "Incompatible Pokemon version ${pokemon.version}"
        }

        if (pokemon.isEmpty) return false

        @Suppress("NAME_SHADOWING")
        val slot = sanitizePokemonSlot(slot)

        // set species id
        data[startOffset + 0x1 + slot] = pokemon.speciesId.toUByte()

        setPokemonData(pokemonData = pokemon.asBytes(), slot = slot)

        // recalculate level from exp & stats if pokemon is moved from party to box
        with(getMutablePokemon(slot)) {
            mutator.experiencePoints(experiencePoints).level(level)
        }

        return true
    }

    private fun sanitizePokemonSlot(slot: Int): Int {
        @Suppress("NAME_SHADOWING")
        var slot = slot
        if (size < capacity) {
            slot = slot.coerceAtMost(size)
            if (slot == size) size++
        }
        return slot.coerceIn(0, capacity - 1)
    }

    private fun setPokemonData(pokemonData: UByteArray, slot: Int) {
        val (dataOfs, trainerNameOfs, nicknameOfs) = getPokemonOffsets(index, startOffset, slot)
        with(pokemonData) {
            // insert pokemon data
            copyInto(
                destination = data,
                destinationOffset = dataOfs,
                startIndex = 0,
                endIndex = Pokemon.DataSizeInBox
            )
            // insert trainer name and nickname
            copyInto(
                destination = data,
                destinationOffset = trainerNameOfs,
                startIndex = Pokemon.DataSizeInBox,
                endIndex = Pokemon.DataSizeInBox + Pokemon.NameMaxSize
            )
            copyInto(
                destination = data,
                destinationOffset = nicknameOfs,
                startIndex = Pokemon.DataSizeInBox + Pokemon.NameMaxSize
            )
        }
    }

    override fun deletePokemon(slot: Int) {
        require(startOffset != 0) { "This box instance is read-only" }
        require(slot in 0 until capacity) { "Pokemon slot $slot is out of bounds" }

        if (slot >= size) return // empty slot

        val endSlot = size - 1

        // In gen 1 there couldn't be empty slots between the pokemon in the storage
        // Move back the pokemon from 1 position if the passed slot isn't the last one
        if (slot < endSlot) {
            TODO("Shift pokemon")
        }

        // erase data
        setPokemonData(pokemonData = UByteArray(Pokemon.FullDataSizeInBox), slot = endSlot)

        // decrease the number of pokemon in the storage
        size--
    }

    companion object {
        internal const val BoxSize = 0x462
        internal const val PartySize = 0x194

        internal const val PokemonDataBoxOffset = 0x16
        internal const val PokemonDataPartyOffset = 0x8

        internal const val TrainerNameBoxOffset = 0x2AA
        internal const val TrainerNamePartyOffset = 0x110

        internal const val NicknameBoxOffset = 0x386
        internal const val NicknamePartyOffset = 0x152

        /**
         * Return offsets respectively for data - trainer name - nickname
         *
         * Use destructuring declarations with the returned Triple instance:
         * - val (data, trainerName, nickname) = getPokemonOffsets(index, startOffset, slot)
         */
        internal fun getPokemonOffsets(
            index: Int,
            startOffset: Int,
            slot: Int
        ): Triple<Int, Int, Int> {
            val dataOffset: Int
            val trainerNameOffset: Int
            val nicknameOffset: Int
            val size: Int

            if (index.isPartyIndex) {
                size = Pokemon.DataSizeInParty
                dataOffset = startOffset + PokemonDataPartyOffset
                nicknameOffset = startOffset + NicknamePartyOffset
                trainerNameOffset = startOffset + TrainerNamePartyOffset
            } else {
                size = Pokemon.DataSizeInBox
                dataOffset = startOffset + PokemonDataBoxOffset
                nicknameOffset = startOffset + NicknameBoxOffset
                trainerNameOffset = startOffset + TrainerNameBoxOffset
            }

            return Triple(
                dataOffset + (size * slot),
                trainerNameOffset + (Pokemon.NameMaxSize * slot),
                nicknameOffset + (Pokemon.NameMaxSize * slot)
            )
        }
    }
}