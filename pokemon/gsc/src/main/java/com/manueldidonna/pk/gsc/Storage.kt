package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.core.Pokemon as CorePokemon

internal class Storage(
    private val data: UByteArray,
    private val startOffset: Int,
    override val version: Version,
    override val index: Int,
    override val capacity: Int,
    override val name: String
) : MutableStorage {

    override var size: Int
        get() = data[startOffset].toInt()
        private set(value) {
            val coercedValue = value.coerceIn(0, capacity)
            data[startOffset] = coercedValue.toUByte()
            data[startOffset + coercedValue + 1] = 0xFF.toUByte()
        }

    override fun getPokemon(slot: Int): CorePokemon {
        require(slot in 0 until capacity) { "Slot $slot is out of bounds [0 - $capacity]" }
        return Pokemon(getPokemonData(slot), index = index, slot = slot, version = version)
    }

    override fun getMutablePokemon(slot: Int): MutablePokemon {
        require(startOffset != 0) { "This box instance is read-only" }
        require(slot in 0 until capacity) { "Slot $slot is out of bounds [0 - $capacity]" }
        return Pokemon(getPokemonData(slot), index = index, slot = slot, version = version)
    }

    private fun getPokemonData(slot: Int): UByteArray {
        if (slot >= size) return UByteArray(PokemonSizeInBox + 11 * 2)
        val (dataOfs, trainerNameOfs, nickOfs) = getPokemonOffsets(slot)
        return UByteArray(PokemonSizeInBox + 11 * 2).apply {
            // Copy Pokemon Box Data
            data.copyInto(this, 0, dataOfs, dataOfs + PokemonSizeInBox)
            // Copy Trainer Name
            data.copyInto(this, PokemonSizeInBox, trainerNameOfs, trainerNameOfs + 11)
            // Copy Pokemon Name
            data.copyInto(this, PokemonSizeInBox + 11, nickOfs, nickOfs + 11)
        }
    }

    /**
     * Return offsets respectively for data - trainer name - nickname
     *
     * Use destructuring declarations with the returned Triple instance:
     * - val (data, trainerName, nickname) = getPokemonOffsets(index, startOffset, slot)
     */
    private fun getPokemonOffsets(slot: Int): Triple<Int, Int, Int> {
        val size: Int = if (index.isPartyIndex) PokemonSizeInParty else PokemonSizeInBox
        val dataOffset = startOffset + (capacity + 2) + (slot * size)
        val trainerNameOffset = startOffset + (capacity + 2) + (size * capacity) + (slot * 11)
        val nicknameOffset = trainerNameOffset + (capacity - slot) * 11 + (slot * 11)
        return Triple(dataOffset, trainerNameOffset, nicknameOffset)
    }

    override fun insertPokemon(pokemon: CorePokemon, slot: Int): Boolean {
        require(slot in 0 until capacity) { "Slot $slot is out of bounds [0 - ${capacity - 1}" }
        require(pokemon.version.isSecondGeneration) { "Unsupported version: ${pokemon.version}" }

        if (pokemon.isEmpty) return false

        // set species id
        data[startOffset + 1 + slot] = pokemon.speciesId.toUByte()

        // increase size if needed
        if (slot >= size) size++

        @Suppress("NAME_SHADOWING")
        val slot = slot.coerceAtMost(size - 1)

        setPokemonData(pokemonData = pokemon.asBytes(), slot = slot)

        // calculate stats if pokemon is moved from box to party
        if (index.isPartyIndex) {
            Pokemon.moveToParty(pokemon, data, getPokemonOffsets(slot).first)
        }

        return true
    }

    private fun setPokemonData(pokemonData: UByteArray, slot: Int) {
        val (dataOfs, trainerNameOfs, nicknameOfs) = getPokemonOffsets(slot)
        with(pokemonData) {
            // set pokemon data
            copyInto(
                destination = data,
                destinationOffset = dataOfs,
                startIndex = 0,
                endIndex = PokemonSizeInBox
            )
            // set trainer name
            copyInto(data, trainerNameOfs, PokemonSizeInBox, PokemonSizeInBox + 11)
            // set nickname
            copyInto(data, nicknameOfs, PokemonSizeInBox + 11)
        }
    }

    override fun deletePokemon(slot: Int) {
        TODO("Not yet implemented")
    }

    companion object {
        private const val PokemonSizeInParty = 48
        private const val PokemonSizeInBox = 32
    }
}