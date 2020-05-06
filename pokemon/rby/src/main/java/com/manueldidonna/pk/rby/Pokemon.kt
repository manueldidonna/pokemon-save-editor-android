package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.rby.utils.getGameBoyString
import com.manueldidonna.pk.rby.utils.readBigEndianInt
import com.manueldidonna.pk.rby.utils.readBigEndianUShort

/**
 * 0x00 0x1 - species ID
 * 0x03 0x1 - level
 * 0x0C 0x2 - trainer ID
 * 0x0E 0x3 - experience points
 * 0x1b 0x2 - ivs (4 bits for each IV excluding HP)
 * 0x21 0xb - trainer name
 * 0x2C 0xb - pokemon name
 */
internal class Pokemon(private val data: UByteArray, box: Int, slot: Int) : Pokemon {

    init {
        require(data.size == 0x37) { "Incorrect size for pokemon data" }
    }

    override val position by lazy { Pokemon.Position(box, slot) }

    override val speciesId: Int
        get() = data[0].toInt()

    override val nickname: String
        get() = getGameBoyString(data, startOffset = 0x2C, stringLength = 11, isJapanese = false)

    override val level: Int
        get() = data[0x03].toInt()

    override val experiencePoints: Int
        get() = data.readBigEndianInt(0xE) shr 8

    /**
     * This value doesn't exist in gen1 but the pokemon bank derives it from the exp. points
     */
    override val natureId: Int
        get() = experiencePoints % 25

    override val trainerId: UInt
        get() = data.readBigEndianUShort(0x0C).toUInt()

    override val trainerName: String
        get() = getGameBoyString(data, startOffset = 0x21, stringLength = 11, isJapanese = false)

    /**
     * [Pokemon.IndividualValues.specialDefense] and [Pokemon.IndividualValues.specialAttack] are equal
     */
    override val iV: Pokemon.IndividualValues by lazy {
        object : Pokemon.IndividualValues {

            private val DV16: Int = data.readBigEndianUShort(0x1b).toInt()

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
}