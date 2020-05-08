package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.PokemonWriter
import com.manueldidonna.pk.rby.utils.setGameBoyString
import com.manueldidonna.pk.rby.utils.writeBidEndianShort

internal class PokemonWriter(
    private val data: UByteArray,
    private val speciesIdOffset: Int,
    private val dataOffset: Int,
    private val trainerNameOffset: Int,
    private val pokemonNameOffset: Int
) : PokemonWriter {

    override fun speciesId(value: Int): PokemonWriter = apply {
        // TODO: value must be a national id. Convert to  g1 species id. Set type 1 and 2
        // data[speciesIdOffset] = value.toUByte()
        // data[dataOffset] = value.toUByte()
    }

    override fun trainerId(value: UInt): PokemonWriter = apply {
        data.writeBidEndianShort(dataOffset + 0xC, value.toShort())
    }

    override fun nickname(value: String): PokemonWriter = apply {
        setGameBoyString(value, 10, false, outputDataSize = 11).copyInto(data, pokemonNameOffset)
    }

    override fun trainerName(value: String): PokemonWriter = apply {
        setGameBoyString(value, 7, false, outputDataSize = 11).copyInto(data, trainerNameOffset)
    }
}