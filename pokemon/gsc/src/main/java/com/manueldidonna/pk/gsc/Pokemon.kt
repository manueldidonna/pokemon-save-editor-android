package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.core.Trainer.Gender
import com.manueldidonna.pk.gsc.converter.getUniversalItemId
import com.manueldidonna.pk.resources.calculateStatistics
import com.manueldidonna.pk.resources.getBaseStatistics
import com.manueldidonna.pk.utils.getStringFromGameBoyData
import com.manueldidonna.pk.utils.readBigEndianInt
import com.manueldidonna.pk.utils.readBigEndianUShort
import com.manueldidonna.pk.utils.writeBidEndianShort
import com.manueldidonna.pk.core.Pokemon as CorePokemon

/**
 * 0x00 0x1 - species ID
 * 0x01 0x1 - held item ID
 * 0x02 0x1 - first move ID
 * 0x03 0x1 - second move ID
 * 0x04 0x1 - third move ID
 * 0x05 0x1 - fourth move ID
 * 0x06 0x2 - trainer ID
 * 0x08 0x3 - experience points
 * 0x0B 0xA - evs (2 bytes for each EV: hp, attack, defense, speed & special)
 * 0x15 0x2 - ivs (4 bits for each IV excluding HP)
 * ------------------------
 * (2 bits for applied pp-ups, 6 bits for current PPs)
 * 0x17	0x1 - Move 1's PP values
 * 0x18	0x1 - Move 2's PP values
 * 0x19	0x1 - Move 3's PP values
 * 0x1A	0x1 - Move 4's PP values
 * ------------------------
 * 0x1B 0x1 - friendship
 * 0x1C 0x1 - pokerus
 * 0x1D 0x2 - caught data
 * ----- CAUGHT DATA ---------
 * Time and level
 * - 2 bits: Time of day (1: morning, 2: day, 3: night)
 * - 6 bits: Level
 * OT gender and location
 * - 1 bit: OT Gender (0: Male, 1: Female)
 * - 7 bits: Location
 * ------------------------
 * 0x1F 0x1 - level
 * -------- PARTY ---------
 * 0x22 0x2 - current HP
 * 0x24 0x2 - maximum HP
 * 0x26 0x2 - attack stat
 * 0x28 0x2 - defense stat
 * 0x2A 0x2 - speed stat
 * 0x2C 0x2 - special attack stat
 * 0x2E 0x2 - special defense stat
 * ------------------------
 * 0x20 0xb - trainer name
 * 0x2B 0xb - pokemon name
 *
 * Gen1 games store the names in a different location of the pokemon box
 * For a mutable instance the real offsets are [trainerNameOffset] & [pokemonNameOffset]
 *
 */
internal class Pokemon(
    private val data: UByteArray,
    override val version: Version,
    index: Int,
    slot: Int
) : MutablePokemon {

    override fun asBytes(): UByteArray {
        return data.copyOf()
    }

    override fun asMutablePokemon(): MutablePokemon {
        return this
    }

    override val mutator: MutablePokemon.Mutator by lazy { Mutator(this, data) }

    override val position by lazy { CorePokemon.Position(index, slot) }

    override val trainer: Trainer
        get() = Trainer(
            name = getStringFromGameBoyData(data, 0x20, 0xB, false),
            visibleId = data.readBigEndianUShort(0x6).toInt(),
            secretId = 0,
            gender = getTrainerGender()
        )

    private fun getTrainerGender(): Trainer.Gender {
        // Trainer gender is exclusive to Crystal
        if (version != Version.Crystal) return Gender.Male
        val caughtData = data.readBigEndianUShort(0x1D).toInt()
        return if (caughtData ushr 7 and 1 == 0) Gender.Male else Gender.Female
    }

    override val isShiny: Boolean
        get() {
            with(iV) {
                if (speed != 10) return false
                if (specialAttack != 10) return false
                if (defense != 10) return false
                return attack and 2 == 2
            }
        }

    override val speciesId: Int
        get() = data[0].toInt()

    override val nickname: String
        get() = getStringFromGameBoyData(data, 0x2B, 0xB, false)

    override val level: Int
        get() = data[0x1F].toInt()

    override val experiencePoints: Int
        get() = data.readBigEndianInt(0xE) ushr 8

    /**
     * This value doesn't exist in gen1 but the pokemon bank derives it from the exp. points
     */
    override val natureId: Int
        get() = experiencePoints % 25

    override val friendship: Property<Int>
        get() = data[0x1B].toInt().asProperty()

    override fun <T> selectMove(index: Int, mapTo: (id: Int, powerPoints: Int, ups: Int) -> T): T {
        require(index in 0..3) { "Move index is out of bounds [0 - 3]" }
        return mapTo(
            data[0x02 + index].toInt(),
            data[0x17 + index].toInt() and 0x3F,
            (data[0x17 + index].toInt() and 0xC0) ushr 6
        )
    }

    override val heldItemId: Property<Int>
        get() = getUniversalItemId(data[0x1].toInt()).asProperty()

    override val pokerus: Property<Pokerus>
        get() {
            val strain = data[0x1C].toInt() ushr 4
            val days = data[0x1C].toInt() and 0xF
            val pokerus =
                if (strain !in Pokerus.StrainValues) Pokerus.NeverInfected
                else Pokerus(
                    strain = strain,
                    days = days.coerceIn(0, Pokerus.maxAllowedDays(strain))
                )
            return pokerus.asProperty()
        }

    override val iV: CorePokemon.StatisticValues by lazy {
        object : CorePokemon.StatisticValues {
            private val DV16: Int
                get() = data.readBigEndianUShort(0x15).toInt()

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
                get() = data.readBigEndianUShort(0x0B).toInt()

            override val attack: Int
                get() = data.readBigEndianUShort(0x0D).toInt()

            override val defense: Int
                get() = data.readBigEndianUShort(0x0F).toInt()

            override val speed: Int
                get() = data.readBigEndianUShort(0x11).toInt()

            override val specialAttack: Int
                get() = data.readBigEndianUShort(0x13).toInt()

            override val specialDefense: Int
                get() = specialAttack
        }
    }

    override val form: CorePokemon.Form?
        get() {
            // only Unown has multiple forms
            if (speciesId != 201) return null
            return CorePokemon.Form.Unown(letter = getUnownLetter(this))
        }

    companion object {
        internal fun getUnownLetter(pokemon: CorePokemon): Char {
            val letterIndex = with(pokemon.iV) {
                var letterIndex = 0
                letterIndex = letterIndex or (attack and 0x6 shl 5)
                letterIndex = letterIndex or (defense and 0x6 shl 3)
                letterIndex = letterIndex or (speed and 0x6 shl 1)
                letterIndex = letterIndex or (specialAttack and 0x6 shr 1)
                letterIndex / 10
            }
            require(letterIndex in 0..25) { "Unexpected Unown letter index $letterIndex" }
            return (letterIndex + 'A'.toInt()).toChar()
        }

        internal fun moveToParty(pokemon: CorePokemon, into: UByteArray, pokemonOffset: Int) {
            val stats: CorePokemon.StatisticValues = with(pokemon) {
                val base = getBaseStatistics(speciesId, version)
                calculateStatistics(level, base, iV, eV, version)
            }

            fun setStat(offset: Int, value: Int) {
                into.writeBidEndianShort(pokemonOffset + offset, value.toShort())
            }

            // update current HP
            into.writeBidEndianShort(pokemonOffset + 0x22, stats.health.toShort())

            // update current stats
            setStat(offset = 0x24, value = stats.health)
            setStat(offset = 0x26, value = stats.attack)
            setStat(offset = 0x28, value = stats.defense)
            setStat(offset = 0x2A, value = stats.speed)
            setStat(offset = 0x2C, value = stats.specialAttack)
            setStat(offset = 0x2E, value = stats.specialDefense)
        }
    }
}