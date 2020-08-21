package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.rby.Pokemon.Companion.DataSizeInBox
import com.manueldidonna.pk.rby.Pokemon.Companion.NameMaxSize
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

    override var size: Int
        get() = data[0].toInt()
        private set(value) {
            val coercedValue = value.coerceIn(0, capacity)
            data[0] = (coercedValue).toUByte()
            data[coercedValue + 1] = 0xFF.toUByte()
        }

    override fun get(index: Int): com.manueldidonna.pk.core.Pokemon {
        require(index in 0 until capacity) { "Index $index is out of bounds [0 - $capacity]" }
        return Pokemon(version, getPokemonData(index), storageIndex, index)
    }

    override fun set(index: Int, pokemon: CorePokemon) {
        require(index in 0 until capacity) {
            "Index $index is out of bounds [0 - ${capacity - 1}]"
        }
        require(pokemon.version.isFirstGeneration) {
            "Unsupported pokemon version: ${pokemon.version}"
        }

        if (pokemon.isEmpty) {
            removeAt(index)
            return
        }

        @Suppress("NAME_SHADOWING")
        val pokemon = pokemon.toMutablePokemon()

        // update level if pokemon is moved from party to box
        if (pokemon.position.storageIndex.isPartyIndex && !storageIndex.isPartyIndex) {
            pokemon.mutator.experiencePoints(pokemon.experiencePoints).level(pokemon.level)
        }

        // increase size if needed
        if (index >= size) size++

        @Suppress("NAME_SHADOWING")
        val index = index.coerceAtMost(size - 1)

        // set species id
        data[1 + index] = pokemon.speciesId.toUByte()

        setPokemonData(pokemon.exportToBytes(), index)

        // recalculate level from exp & stats if pokemon is moved from box to party
        if (storageIndex.isPartyIndex) {
            Pokemon.moveToParty(pokemon, data, getPokemonOffsets(index).first)
        }
    }

    override fun removeAt(index: Int): com.manueldidonna.pk.core.Pokemon {
        TODO("Not yet implemented")
    }

    /**
     * Return offsets respectively for data - trainer name - nickname
     *
     * Use destructuring declarations with the returned Triple instance:
     * - val (data, trainerName, nickname) = getPokemonOffsets(index, startOffset, slot)
     */
    private fun getPokemonOffsets(slot: Int): Triple<Int, Int, Int> {
        val dataOffset: Int
        val trainerNameOffset: Int
        val nicknameOffset: Int
        val size: Int

        if (storageIndex.isPartyIndex) {
            size = Pokemon.DataSizeInParty
            dataOffset = PokemonDataPartyOffset
            nicknameOffset = NicknamePartyOffset
            trainerNameOffset = TrainerNamePartyOffset
        } else {
            size = DataSizeInBox
            dataOffset = PokemonDataBoxOffset
            nicknameOffset = NicknameBoxOffset
            trainerNameOffset = TrainerNameBoxOffset
        }

        return Triple(
            dataOffset + (size * slot),
            trainerNameOffset + (NameMaxSize * slot),
            nicknameOffset + (NameMaxSize * slot)
        )
    }

    private fun getPokemonData(index: Int): UByteArray {
        if (index >= size) return UByteArray(Pokemon.FullDataSizeInBox)
        val (dataOfs, trainerNameOfs, nickOfs) = getPokemonOffsets(index)
        return UByteArray(Pokemon.FullDataSizeInBox).apply {
            // Copy Pokemon Box Data
            data.copyInto(this, 0, dataOfs, dataOfs + DataSizeInBox)
            // Copy Trainer Name
            data.copyInto(this, DataSizeInBox, trainerNameOfs, trainerNameOfs + NameMaxSize)
            // Copy Pokemon Name
            data.copyInto(this, DataSizeInBox + NameMaxSize, nickOfs, nickOfs + NameMaxSize)
        }
    }

    private fun setPokemonData(pokemonData: UByteArray, slot: Int) {
        val (dataOfs, trainerNameOfs, nicknameOfs) = getPokemonOffsets(slot)
        with(pokemonData) {
            // insert pokemon data
            copyInto(data, dataOfs, 0, DataSizeInBox)
            // insert trainer name and nickname
            copyInto(data, trainerNameOfs, DataSizeInBox, DataSizeInBox + NameMaxSize)
            copyInto(data, nicknameOfs, DataSizeInBox + NameMaxSize)
        }
    }

    override fun toMutableStorage(): MutableStorage {
        return Storage(
            data = data,
            storageIndex = storageIndex,
            capacity = capacity,
            version = version,
            name = name
        )
    }

    override fun exportToBytes(): UByteArray {
        return data.copyOf()
    }

    companion object {
        private const val PokemonDataBoxOffset = 0x16
        private const val PokemonDataPartyOffset = 0x8

        private const val TrainerNameBoxOffset = 0x2AA
        private const val TrainerNamePartyOffset = 0x110

        private const val NicknameBoxOffset = 0x386
        private const val NicknamePartyOffset = 0x152
    }
}