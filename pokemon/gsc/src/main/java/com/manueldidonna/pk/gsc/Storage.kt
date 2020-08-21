package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.core.Pokemon as CorePokemon

internal class Storage(
    private val data: UByteArray,
    private val startOffset: Int,
    private val version: Version,
    private val storageIndex: Int,
    override val capacity: Int,
    override val name: String,
) : MutableStorage {

    override var size: Int
        get() = data[startOffset].toInt()
        private set(value) {
            val coercedValue = value.coerceIn(0, capacity)
            data[startOffset] = coercedValue.toUByte()
            data[startOffset + coercedValue + 1] = 0xFF.toUByte()
        }

    override fun get(index: Int): CorePokemon {
        require(index in 0 until capacity) {
            "Index $index is out of bounds [0 - ${capacity - 1}]"
        }
        return Pokemon(getPokemonData(index), version, storageIndex, index)
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
    private fun getPokemonOffsets(index: Int): Triple<Int, Int, Int> {
        val size: Int = if (storageIndex.isPartyIndex) PokemonSizeInParty else PokemonSizeInBox
        val dataOffset = startOffset + (capacity + 2) + (index * size)
        val trainerNameOffset = startOffset + (capacity + 2) + (size * capacity) + (index * 11)
        val nicknameOffset = trainerNameOffset + (capacity - index) * 11 + (index * 11)
        return Triple(dataOffset, trainerNameOffset, nicknameOffset)
    }

    override fun set(index: Int, pokemon: CorePokemon) {
        require(index in 0 until capacity) {
            "Index $index is out of bounds [0 - ${capacity - 1}]"
        }
        require(pokemon.version.isSecondGeneration) {
            "Unsupported pokemon version: ${pokemon.version}"
        }
        if (pokemon.isEmpty) {
            removeAt(index)
            return
        }

        // set species id
        data[startOffset + 1 + index] = pokemon.speciesId.toUByte()

        // increase size if needed
        if (index >= size) size++

        @Suppress("NAME_SHADOWING")
        val index = index.coerceAtMost(size - 1)

        setPokemonData(pokemon.exportToBytes(), index)

        // calculate stats if pokemon is moved from box to party
        if (storageIndex.isPartyIndex) {
            Pokemon.moveToParty(pokemon, data, getPokemonOffsets(index).first)
        }
    }


    private fun setPokemonData(pokemonData: UByteArray, index: Int) {
        val (dataOfs, trainerNameOfs, nicknameOfs) = getPokemonOffsets(index)
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

    override fun removeAt(index: Int): CorePokemon {
        TODO("Not yet implemented")
    }

    companion object {
        private const val PokemonSizeInParty = 48
        private const val PokemonSizeInBox = 32
    }
}