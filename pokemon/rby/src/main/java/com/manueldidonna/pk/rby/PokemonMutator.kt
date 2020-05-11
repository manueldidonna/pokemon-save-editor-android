package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.utils.getExperienceGroup
import com.manueldidonna.pk.core.utils.getExperiencePoints
import com.manueldidonna.pk.rby.utils.*

internal class PokemonMutator(
    private val data: UByteArray,
    private val dataOffset: Int,
    private val trainerNameOffset: Int,
    private val pokemonNameOffset: Int,
    private val isPartyPokemon: Boolean
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

    override fun experiencePoints(value: Int): MutablePokemon.Mutator = apply {
        data.writeBidEndianInt(dataOffset + 0xE, value shl 8, write3Bytes = true)
    }

    override fun level(value: Int): MutablePokemon.Mutator = apply {
        val coercedLevel = value.coerceIn(1, 100)
        data[dataOffset + 0x3] = coercedLevel.toUByte()
        if(isPartyPokemon) {
            data[dataOffset + 0x21] = coercedLevel.toUByte()
        }
        val speciesId = getGameBoySpecies(data[dataOffset].toInt()) // TODO: this is an hack
        experiencePoints(getExperiencePoints(value, getExperienceGroup(speciesId)))
    }

    override fun moveId(id: Int, moveIndex: Int): MutablePokemon.Mutator = apply {
        require(moveIndex in 0..3) { "Move index must be in 0..3" }
        data[dataOffset + 0x08 + moveIndex] = id.toUByte()
        movePowerPoints(moveIndex = moveIndex, moveId = id, points = 0)
    }

    override fun movePowerPoints(
        moveIndex: Int,
        moveId: Int,
        points: Int
    ): MutablePokemon.Mutator = apply {
        require(moveIndex in 0..3) { "Move index must be in 0..3" }
        val ppIndex = dataOffset + 0X1D + moveIndex
        val realPoints = if (moveId > 0) getPoiwerPoints(moveId) else points.coerceAtMost(63)
        data[ppIndex] = (data[ppIndex] and 0xC0u) or realPoints.toUByte()
    }
}
