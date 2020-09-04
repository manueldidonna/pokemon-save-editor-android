package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.rby.converter.getNationalSpecies
import com.manueldidonna.pk.resources.calculateStatistics
import com.manueldidonna.pk.resources.getBaseStatistics
import com.manueldidonna.pk.resources.getExperienceGroup
import com.manueldidonna.pk.resources.getLevel
import com.manueldidonna.pk.utils.getStringFromGameBoyData
import com.manueldidonna.pk.utils.readBigEndianInt
import com.manueldidonna.pk.utils.readBigEndianUShort
import com.manueldidonna.pk.utils.writeBidEndianShort
import com.manueldidonna.pk.core.Pokemon as CorePokemon

/**
 * 0x00 0x1 - species ID
 * 0x01 0x2 - current HP
 * 0x03 0x1 - level
 * 0x04 0x1 - status condition
 * 0x05 0x1 - type 1
 * 0x06 0x1 - type 2
 * 0x07 0x1 - catch rate/held item (for gen 2 compatibility)
 * 0x08 0x1 - Index number of move 1
 * 0x09 0x1 - Index number of move 2
 * 0x0A 0x1 - Index number of move 3
 * 0x0B 0x1 - Index number of move 4
 * 0x0C 0x2 - trainer ID
 * 0x0E 0x3 - experience points
 * 0x11 0xA - evs (2 bytes for each EV: hp, attack, defense, speed & special)
 * 0x1b 0x2 - ivs (4 bits for each IV excluding HP)
 * ------------------------
 * (2 bits for applied pp-ups, 6 bits for current PPs)
 * 0x1D	0x1 - Move 1's PP values
 * 0x1E	0x1 - Move 2's PP values
 * 0x1F	0x1 - Move 3's PP values
 * 0x20	0x1 - Move 4's PP values
 * ------------------------
 * -------- PARTY ---------
 * 0x21 0x1 - level (exclusive of the pokemon in the party. @see [level])
 * 0x22 0x2 - maximum HP
 * 0x24 0x2 - attack stat
 * 0x26 0x2 - defense stat
 * 0x28 0x2 - speed stat
 * 0x2A 0x2 - special stat
 * ------------------------
 * 0x21 0xb - trainer name
 * 0x2C 0xb - pokemon name
 */
internal class Pokemon(
    override val version: Version,
    private val data: UByteArray,
    private val storageIndex: Int,
    pokemonIndex: Int,
) : MutablePokemon {

    override fun exportToBytes(): UByteArray {
        return data.copyOf()
    }

    override fun toMutablePokemon(): MutablePokemon {
        return Pokemon(version, exportToBytes(), position.storageIndex, position.pokemonIndex)
    }

    override val form: CorePokemon.Form? = null

    override val position by lazy { CorePokemon.Position(storageIndex, pokemonIndex) }

    override val trainer: Trainer
        get() = Trainer(
            name = getStringFromGameBoyData(data, 0x21, 11, false),
            visibleId = data.readBigEndianUShort(0x0C).toInt(),
            secretId = 0, // unused in gen 1
            gender = Trainer.Gender.Male
        )

    override val speciesId: Int
        get() = getNationalSpecies(data[0].toInt())

    override val nickname: String
        get() = getStringFromGameBoyData(data, 0x2C, 11, false)

    override val level: Int
        get() {
            return when {
                // fast path, box pokemon. Read from the level offset
                !storageIndex.isPartyIndex -> data[0x3].toInt()
                // party offsets aren't available. Calculate the level from the exp
                else -> getLevel(experiencePoints, getExperienceGroup(speciesId))
            }
        }

    override val experiencePoints: Int
        get() = data.readBigEndianInt(0xE) ushr 8

    /**
     * This value doesn't exist in gen1 but the pokemon bank derives it from the exp. points
     */
    override val natureId: Int
        get() = experiencePoints % 25

    /**
     * This property han been introduced in the second gen. games. When this pokemon is transf
     */
    override val isShiny: Boolean
        get() {
            with(iV) {
                if (speed != 10) return false
                if (specialAttack != 10) return false
                if (defense != 10) return false
                return attack and 2 == 2
            }
        }

    override fun <T> selectMove(index: Int, mapTo: (id: Int, powerPoints: Int, ups: Int) -> T): T {
        require(index in 0..3) { "Move index is out of bounds [0 - 3]" }
        return mapTo(
            data[0x08 + index].toInt(),
            data[0x1D + index].toInt() and 0x3F,
            (data[0x1D + index].toInt() and 0xC0) ushr 6
        )
    }

    /**
     * [CorePokemon.StatisticValues.specialDefense] is equal to [CorePokemon.StatisticValues.specialAttack]
     */
    override val iV: CorePokemon.StatisticValues by lazy {
        object : CorePokemon.StatisticValues {
            private val DV16: Int
                get() = data.readBigEndianUShort(0x1b).toInt()

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

    override val eV: CorePokemon.StatisticValues by lazy {
        object : CorePokemon.StatisticValues {
            override val health: Int
                get() = data.readBigEndianUShort(0x11).toInt()

            override val attack: Int
                get() = data.readBigEndianUShort(0x13).toInt()

            override val defense: Int
                get() = data.readBigEndianUShort(0x15).toInt()

            override val speed: Int
                get() = data.readBigEndianUShort(0x17).toInt()

            override val specialAttack: Int
                get() = data.readBigEndianUShort(0x19).toInt()

            override val specialDefense: Int
                get() = specialAttack
        }
    }

    override val heldItemId: Int? = null
    override val friendship: Int? = null
    override val pokerus: Pokerus? = null
    override val metInfo: MetInfo? = null

    override val mutator: MutablePokemon.Mutator by lazy { Mutator(this, data) }

    companion object {
        internal val NonShinyAttackValues = listOf(1, 4, 5, 8, 9, 12, 13)

        internal fun moveToParty(pokemon: CorePokemon, into: UByteArray, pokemonOffset: Int) {
            // update level
            val stats: CorePokemon.StatisticValues = with(pokemon) {
                val base = getBaseStatistics(speciesId, version)
                calculateStatistics(level, base, iV, eV, version)
            }

            fun setStat(offset: Int, value: Int) {
                into.writeBidEndianShort(pokemonOffset + offset, value.toShort())
            }

            // calculate level from exp
            into[pokemonOffset + 0x21] = pokemon.run {
                getLevel(experiencePoints, getExperienceGroup(speciesId)).toUByte()
            }

            // remove any status
            into[0x04] = 0u

            // update current HP
            setStat(0x1, stats.health)

            // update current stats
            setStat(offset = 0x22, value = stats.health)
            setStat(offset = 0x24, value = stats.attack)
            setStat(offset = 0x26, value = stats.defense)
            setStat(offset = 0x28, value = stats.speed)
            setStat(offset = 0x2A, value = stats.specialAttack)
        }
    }
}
