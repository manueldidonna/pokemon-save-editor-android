package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.StorageIndex
import com.manueldidonna.pk.core.isParty
import com.manueldidonna.pk.core.utils.getExperienceGroup
import com.manueldidonna.pk.core.utils.getExperiencePoints
import com.manueldidonna.pk.core.utils.getLevel
import com.manueldidonna.pk.rby.utils.*

/**
 * 0x00 0x1 - species ID
 * 0x03 0x1 - level
 * 0x08 0x1 - Index number of move 1
 * 0x09 0x1 - Index number of move 2
 * 0x0A 0x1 - Index number of move 3
 * 0x0B 0x1 - Index number of move 4
 * 0x0C 0x2 - trainer ID
 * 0x0E 0x3 - experience points
 * 0x1b 0x2 - ivs (4 bits for each IV excluding HP)
 * ------------------------
 * (2 bits for applied pp-ups, 6 bits for current PPs)
 * 0x1D	0x1 - Move 1's PP values
 * 0x1E	0x1 - Move 2's PP values
 * 0x1F	0x1 - Move 3's PP values
 * 0x20	0x1 - Move 4's PP values
 * ------------------------
 * 0x21 0x1 - level (exclusive of the pokemon in the party. @see [level])
 * ------------------------
 * 0x21 0xb - trainer name
 * 0x2C 0xb - pokemon name
 *
 * Gen1 games store the names in a different location of the pokemon box
 * For a mutable instance the real offsets are [trainerNameOffset] & [pokemonNameOffset]
 *
 */
internal class Pokemon(
    private val data: UByteArray,
    private val startOffset: Int,
    private val trainerNameOffset: Int,
    private val pokemonNameOffset: Int,
    private val index: StorageIndex,
    slot: Int
) : MutablePokemon {

    companion object {
        fun newImmutableInstance(data: UByteArray, index: StorageIndex, slot: Int): Pokemon {
            return Pokemon(data, 0, PokemonDataSize, PokemonDataSize + NameSize, index, slot)
        }
    }

    init {
        require(startOffset != 0 || data.size == PokemonSize) {
            "This instance is neither mutable or immutable"
        }
    }

    override val position by lazy { Pokemon.Position(index, slot) }

    override val speciesId: Int
        get() = getGameBoySpecies(data[startOffset].toInt())

    override val nickname: String
        get() = getGameBoyString(data, pokemonNameOffset, stringLength = 11, isJapanese = false)

    override val level: Int
        get() {
            return when {
                // fast path, box pokemon. Read from the level offset
                !index.isParty -> data[startOffset + 0x3].toInt()
                // mutable pokemon, read value in the party-only level offset
                startOffset != 0 -> data[startOffset + 0x21].toInt()
                // not mutable, party offsets aren't available. Calculate the level from the exp
                else -> getLevel(experiencePoints, getExperienceGroup(speciesId))
            }
        }

    override val experiencePoints: Int
        get() = data.readBigEndianInt(startOffset + 0xE) ushr 8

    /**
     * This value doesn't exist in gen1 but the pokemon bank derives it from the exp. points
     */
    override val natureId: Int
        get() = experiencePoints % 25

    override val trainerId: UInt
        get() = data.readBigEndianUShort(startOffset + 0x0C).toUInt()

    override val trainerName: String
        get() = getGameBoyString(data, trainerNameOffset, stringLength = 11, isJapanese = false)

    override val moves: Pokemon.Moves by lazy {
        object : Pokemon.Moves {
            override fun getId(index: Int): Int {
                require(index in 0..3) { "Move index must be in 0..3" }
                return data[startOffset + 0x08 + index].toInt()
            }

            override fun getPowerPoints(index: Int): Int {
                require(index in 0..3) { "Move index must be in 0..3" }
                return data[startOffset + 0x1D + index].toInt() and 0x3F
            }
        }
    }

    /**
     * [Pokemon.IndividualValues.specialDefense] and [Pokemon.IndividualValues.specialAttack] are equal
     */
    override val iV: Pokemon.IndividualValues by lazy {
        object : Pokemon.IndividualValues {

            private val DV16: Int = data.readBigEndianUShort(startOffset + 0x1b).toInt()

            override val health: Int
                get() = (attack and 1 shl 3) or (defense and 1 shl 2) or (speed and 1 shl 1) or (specialAttack and 1 shl 0)

            override val attack: Int
                get() = DV16 shr 12 and 0xF

            override val defense: Int
                get() = DV16 shr 8 and 0xF

            override val speed: Int
                get() = DV16 shr 4 and 0xF

            override val specialAttack: Int
                get() = DV16 shr 0 and 0xF

            override val specialDefense: Int
                get() = specialAttack
        }
    }

    override val mutator: MutablePokemon.Mutator by lazy { Mutator() }

    inner class Mutator : MutablePokemon.Mutator {

        init {
            require(startOffset != 0) { "This Pokemon instance is read-only" }
        }

        override fun speciesId(value: Int): MutablePokemon.Mutator = apply {
            // TODO: value must be a national id. Convert to  g1 species id. Set type 1 and 2
            // data[speciesIdOffset] = value.toUByte()
            // data[dataOffset] = value.toUByte()
        }

        override fun trainerId(value: UInt): MutablePokemon.Mutator = apply {
            data.writeBidEndianShort(startOffset + 0xC, value.toShort())
        }

        override fun nickname(value: String): MutablePokemon.Mutator = apply {
            setGameBoyString(value, 10, false, 11).copyInto(data, pokemonNameOffset)
        }

        override fun trainerName(value: String): MutablePokemon.Mutator = apply {
            setGameBoyString(value, 7, false, 11).copyInto(data, trainerNameOffset)
        }

        override fun experiencePoints(value: Int): MutablePokemon.Mutator = apply {
            data.writeBidEndianInt(startOffset + 0xE, value shl 8, write3Bytes = true)
            val newLevel = getLevel(value, getExperienceGroup(speciesId))
            if (newLevel != level) {
                level(newLevel)
            }
        }

        override fun level(value: Int): MutablePokemon.Mutator = apply {
            val coercedLevel = value.coerceIn(1, 100)
            data[startOffset + 0x3] = coercedLevel.toUByte()
            if (index.isParty) {
                data[startOffset + 0x21] = coercedLevel.toUByte()
            }
            val experienceGroup = getExperienceGroup(speciesId)
            val levelFromExp = getLevel(experiencePoints, experienceGroup)
            if (levelFromExp != value) {
                experiencePoints(getExperiencePoints(value, experienceGroup))
            }
        }

        override fun moveId(id: Int, moveIndex: Int): MutablePokemon.Mutator = apply {
            require(moveIndex in 0..3) { "Move index must be in 0..3" }
            data[startOffset + 0x08 + moveIndex] = id.toUByte()
            movePowerPoints(moveIndex = moveIndex, moveId = id, points = 0)
        }

        override fun movePowerPoints(
            moveIndex: Int,
            moveId: Int,
            points: Int
        ): MutablePokemon.Mutator = apply {
            require(moveIndex in 0..3) { "Move index must be in 0..3" }
            val ppIndex = startOffset + 0X1D + moveIndex
            val realPoints = if (moveId > 0) getPoiwerPoints(moveId) else points.coerceAtMost(63)
            data[ppIndex] = (data[ppIndex] and 0xC0u) or realPoints.toUByte()
        }
    }
}
