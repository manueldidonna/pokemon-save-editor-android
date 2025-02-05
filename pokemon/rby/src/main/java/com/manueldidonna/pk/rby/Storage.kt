package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.utils.copyIntoFor
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
    private val storageIndex: Int,
    override val capacity: Int,
    private val version: Version,
    override val name: String,
) : MutableStorage {

    override val pokemonFactory: CorePokemon.Factory by lazy { PokemonFactory(version) }

    override var size: Int
        get() = data[0].toInt()
        private set(value) {
            val coercedValue = value.coerceIn(0, capacity)
            data[0] = (coercedValue).toUByte()
            data[coercedValue + 1] = 0xFF.toUByte()
        }

    override fun get(index: Int): CorePokemon? {
        checkPokemonIndex(index)
        if (index >= size) return null
        return Pokemon(
            version = version,
            data = getPokemonData(index),
            storageIndex = storageIndex,
            pokemonIndex = index
        )
    }

    override fun set(index: Int, pokemon: CorePokemon) {
        checkPokemonIndex(index)
        require(!pokemon.isEmpty()) {
            "Pokemon is empty"
        }
        require(pokemon.version.generation == 1) {
            "Unsupported Version: ${pokemon.version}"
        }

        @Suppress("NAME_SHADOWING")
        val index = coerceIndexBySize(index)

        @Suppress("NAME_SHADOWING")
        val pokemon = pokemon.toMutablePokemon()

        // update level if pokemon is moved from party to box
        if (pokemon.position.storageIndex.isPartyIndex && !storageIndex.isPartyIndex) {
            pokemon.mutator.experiencePoints(pokemon.experiencePoints).level(pokemon.level)
        }

        // set species id
        data[1 + index] = pokemon.speciesId.toUByte()

        setPokemonData(pokemon.exportToBytes(), index)

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
            "Index $index is out of bounds [0 - ${capacity - 1}]"
        }
    }

    private fun getPokemonOffsetsByIndex(index: Int): PokemonOffsets {
        val size: Int = if (storageIndex.isPartyIndex) PokemonSizeInParty else PokemonSizeInBox
        val firstByteOffset = capacity + 2 + index * size
        val trainerNameOffset = capacity + 2 + size * capacity + index * 11
        val nicknameOffset = trainerNameOffset + (capacity - index) * 11 + index * 11
        return PokemonOffsets(firstByteOffset, trainerNameOffset, nicknameOffset)
    }

    private fun getPokemonData(index: Int): UByteArray {
        if (index >= size) return UByteArray(PokemonSizeInBoxWithNames)
        val offsets = getPokemonOffsetsByIndex(index)
        return UByteArray(PokemonSizeInBoxWithNames).apply {
            data.copyIntoFor(this, 0, offsets.data, length = PokemonSizeInBox)
            data.copyIntoFor(this, PokemonSizeInBox, offsets.trainerName, length = 11)
            data.copyIntoFor(this, PokemonSizeInBox + 11, offsets.nickname, length = 11)
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
            copyInto(data, offset.nickname, PokemonSizeInBox + 11)
        }
    }

    override fun toMutableStorage(): MutableStorage {
        return Storage(
            data = exportToBytes(),
            storageIndex = storageIndex,
            capacity = capacity,
            version = version,
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
        private const val PokemonSizeInParty = 44
        private const val PokemonSizeInBox = 33
        internal const val PokemonSizeInBoxWithNames = PokemonSizeInBox + 11 + 11
    }
}
