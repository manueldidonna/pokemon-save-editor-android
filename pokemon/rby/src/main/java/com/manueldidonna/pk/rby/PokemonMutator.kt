package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.rby.utils.setGameBoyString
import com.manueldidonna.pk.rby.utils.writeBidEndianShort

internal class PokemonMutator(
    private val data: UByteArray,
    private val dataOffset: Int,
    private val trainerNameOffset: Int,
    private val pokemonNameOffset: Int
) : MutablePokemon.Mutator {

    init {
        require(data.size != PokemonSize && dataOffset != 0) {
            "This Mutator instance has been initialized with an incorrect data"
        }
    }

    override fun speciesId(value: Int): MutablePokemon.Mutator = apply {
        // TODO: value must be a national id. Convert to  g1 species id. Set type 1 and 2
        // data[speciesIdOffset] = value.toUByte()
        // data[dataOffset] = value.toUByte()
    }

    override fun trainerId(value: UInt): MutablePokemon.Mutator = apply {
        data.writeBidEndianShort(dataOffset + 0xC, value.toShort())
    }

    override fun nickname(value: String): MutablePokemon.Mutator = apply {
        setGameBoyString(value, 10, false, outputDataSize = 11).copyInto(data, pokemonNameOffset)
    }

    override fun trainerName(value: String): MutablePokemon.Mutator = apply {
        setGameBoyString(value, 7, false, outputDataSize = 11).copyInto(data, trainerNameOffset)
    }
}