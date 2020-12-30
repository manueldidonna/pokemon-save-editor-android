package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.utils.copyIntoFor
import com.manueldidonna.pk.core.Pokemon as CorePokemon

internal class Storage(
    private val data: UByteArray,
    private val version: Version,
    private val storageIndex: Int,
    override val capacity: Int,
    override val name: String,
) : MutableStorage {

    override val pokemonFactory: CorePokemon.Factory by lazy { PokemonFactory(version) }

    override var size: Int
        get() = data[0].toInt()
        private set(value) {
            val coercedValue = value.coerceIn(0, capacity)
            data[0] = coercedValue.toUByte()
            data[coercedValue + 1] = 0xFF.toUByte()
        }

    override fun get(index: Int): CorePokemon? {
        checkPokemonIndex(index)
        if (index >= size) return null
        return Pokemon(getPokemonData(index), version, storageIndex, index)
    }

    override fun set(index: Int, pokemon: CorePokemon) {
        checkPokemonIndex(index)
        require(!pokemon.isEmpty()) {
            "Pokemon is empty"
        }
        require(pokemon.version.generation == 2) {
            "Unsupported pokemon version: ${pokemon.version}"
        }

        @Suppress("NAME_SHADOWING")
        val index = coerceIndexBySize(index)

        // set species id
        data[1 + index] = pokemon.speciesId.toUByte()

        // set pokemon data
        setPokemonData(pokemon.exportToBytes(), index)

        // calculate stats if pokemon is moved to party
        if (storageIndex.isPartyIndex) {
            Pokemon.setPartyProperties(pokemon, data, getPokemonOffsetsByIndex(index).data)
        }
    }

    private fun coerceIndexBySize(index: Int): Int {
        // increase size if needed
        // size is automatically coerced within its bounds
        if (index >= size) size++
        return index.coerceAtMost(size - 1)
    }

    private fun getPokemonData(index: Int): UByteArray {
        if (index >= size) return UByteArray(PokemonSizeInBoxWithNames)
        val offset = getPokemonOffsetsByIndex(index)
        return UByteArray(PokemonSizeInBoxWithNames).apply {
            data.copyIntoFor(this, 0, offset.data, length = PokemonSizeInBox)
            data.copyIntoFor(this, PokemonSizeInBox, offset.trainerName, length = 11)
            data.copyIntoFor(this, PokemonSizeInBox + 11, offset.nickname, length = 11)
        }
    }

    private fun setPokemonData(pokemonData: UByteArray, index: Int) {
        require(pokemonData.size == PokemonSizeInBoxWithNames) {
            "Invalid Pokemon data size: ${pokemonData.size}"
        }
        val offset = getPokemonOffsetsByIndex(index)
        with(pokemonData) {
            copyIntoFor(data, offset.data, 0, length = PokemonSizeInBox)
            copyIntoFor(data, offset.trainerName, PokemonSizeInBox, length = 11)
            copyIntoFor(data, offset.nickname, PokemonSizeInBox + 11, length = 11)
        }
    }

    private fun getPokemonOffsetsByIndex(index: Int): PokemonOffsets {
        val size = if (storageIndex.isPartyIndex) PokemonSizeInParty else PokemonSizeInBox
        val firstByteOffset = capacity + 2 + index * size
        val trainerNameOffset = capacity + 2 + size * capacity + index * 11
        val nicknameOffset = trainerNameOffset + (capacity - index) * 11 + index * 11
        return PokemonOffsets(firstByteOffset, trainerNameOffset, nicknameOffset)
    }

    override fun removeAt(index: Int) {
        checkPokemonIndex(index)
        if (index >= size) return
        if (index < size - 1) {
            shiftLeftPokemonBytes(index)
        }
        val pokemonSize = if (storageIndex.isPartyIndex) PokemonSizeInParty else PokemonSizeInBox
        val offsetToDelete = getPokemonOffsetsByIndex(size - 1)
        data.fill(0u, offsetToDelete.data, offsetToDelete.data + pokemonSize)
        data.fill(0u, offsetToDelete.trainerName, offsetToDelete.trainerName + 11)
        data.fill(0u, offsetToDelete.nickname, offsetToDelete.nickname + 11)
        size--
    }

    private fun shiftLeftPokemonBytes(index: Int) {
        val pokemonSize = if (storageIndex.isPartyIndex) PokemonSizeInParty else PokemonSizeInBox
        val location = getPokemonOffsetsByIndex(index)
        val first = getPokemonOffsetsByIndex(index + 1)
        val last = getPokemonOffsetsByIndex(size - 1)
        data.copyInto(data, location.data, first.data, last.data + pokemonSize)
        data.copyInto(data, location.nickname, first.nickname, last.nickname + 11)
        data.copyInto(data, location.trainerName, first.trainerName, last.trainerName + 11)
    }

    private fun checkPokemonIndex(index: Int) {
        require(index in 0 until capacity) {
            "Index $index is out of bounds [0..${capacity - 1}]"
        }
    }

    override fun toMutableStorage(): MutableStorage {
        return Storage(
            data = exportToBytes(),
            version = version,
            storageIndex = storageIndex,
            capacity = capacity,
            name = name
        )
    }

    override fun exportToBytes(): UByteArray {
        return data.copyOf()
    }

    private data class PokemonOffsets(
        val data: Int,
        val trainerName: Int,
        val nickname: Int,
    )

    companion object {
        private const val PokemonSizeInParty = 48
        private const val PokemonSizeInBox = 32
        internal const val PokemonSizeInBoxWithNames = PokemonSizeInBox + 11 + 11
    }
}