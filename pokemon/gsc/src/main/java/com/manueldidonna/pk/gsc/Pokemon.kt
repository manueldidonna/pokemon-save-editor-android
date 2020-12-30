package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokemon.CaughtData
import com.manueldidonna.pk.core.Pokerus
import com.manueldidonna.pk.core.Trainer
import com.manueldidonna.pk.core.Trainer.Gender
import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.gsc.converter.getUniversalItemId
import com.manueldidonna.pk.gsc.converter.getUnownLetterFromStatistics
import com.manueldidonna.pk.resources.calculateStatistics
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
 */
internal class Pokemon(
    private val data: UByteArray,
    override val version: Version,
    storageIndex: Int,
    pokemonIndex: Int,
) : MutablePokemon {

    override fun exportToBytes(): UByteArray {
        return data.copyOf()
    }

    override fun toMutablePokemon(): MutablePokemon {
        return Pokemon(exportToBytes(), version, position.storageIndex, position.pokemonIndex)
    }

    override val mutator: MutablePokemon.Mutator by lazy { Mutator(this, data) }

    override val position by lazy { CorePokemon.Position(storageIndex, pokemonIndex) }

    override val trainer: Trainer
        get() = Trainer(
            name = getStringFromGameBoyData(data, 0x20, 0xB, false),
            visibleId = data.readBigEndianUShort(0x6).toInt(),
            secretId = 0,
            gender = getTrainerGender()
        )

    private fun getTrainerGender(): Gender {
        // Trainer gender is exclusive to Crystal
        if (version != Version.Crystal) return Gender.Male
        val caughtData = data.readBigEndianUShort(0x1D).toInt()
        return if (caughtData ushr 7 and 1 == 0) Gender.Male else Gender.Female
    }

    override val isShiny: Boolean
        get() = with(iV) {
            if (speed != 10) return false
            if (specialAttack != 10) return false
            if (defense != 10) return false
            return attack and 2 == 2
        }

    override val speciesId: Int
        get() = data[0].toInt()

    override val nickname: String
        get() = getStringFromGameBoyData(data, 0x2B, 0xB, false)

    override val level: Int
        get() = data[0x1F].toInt()

    override val experiencePoints: Int
        get() = data.readBigEndianInt(0x08) ushr 8

    /**
     * This value doesn't exist in gen1 but the pokemon bank derives it from the exp. points
     */
    override val natureId: Int
        get() = experiencePoints % 25

    override val friendship: Int
        get() = data[0x1B].toInt()

    override fun <M> selectMove(index: Int, mapper: CorePokemon.MoveMapper<M>): M {
        require(index in 0..3) { "Move index is out of bounds [0..3]" }
        return mapper.mapTo(
            id = data[0x02 + index].toInt(),
            powerPoints = data[0x17 + index].toInt() and 0x3F,
            ups = (data[0x17 + index].toInt() and 0xC0) ushr 6
        )
    }

    override val heldItemId: Int
        get() = getUniversalItemId(data[0x1].toInt())

    override val pokerus: Pokerus
        get() {
            val strain = data[0x1C].toInt() ushr 4
            val days = data[0x1C].toInt() and 0xF
            return if (strain !in Pokerus.StrainValues)
                Pokerus.NeverInfected
            else
                Pokerus(
                    strain = strain,
                    days = days.coerceIn(0, Pokerus.maxAllowedDays(strain))
                )
        }

    override val iV: CorePokemon.StatisticValues by lazy {
        object : CorePokemon.StatisticValues {
            private val DV16: Int
                get() = data.readBigEndianUShort(0x15).toInt()

            override val health: Int
                get() = (attack and 1 shl 3) or
                        (defense and 1 shl 2) or
                        (speed and 1 shl 1) or
                        (specialAttack and 1 shl 0)

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
            return CorePokemon.Form.Unown(letter = getUnownLetterFromStatistics(iV = iV))
        }

    override val caughtData: CaughtData?
        get() {
            if (version != Version.Crystal) return null
            val caught = data.readBigEndianUShort(0x1D).toInt()
            val time = when ((caught ushr 14) and 0x3) {
                1 -> CaughtData.Time.TimeOfDay.Morning
                2 -> CaughtData.Time.TimeOfDay.DayTime
                else -> CaughtData.Time.TimeOfDay.Night
            }
            return CaughtData(
                level = caught ushr 8 and 0x3F,
                locationId = caught and 0x7F,
                time = time
            )
        }

    companion object {
        internal fun setPartyProperties(
            pokemon: CorePokemon,
            writeInto: UByteArray,
            dataOffset: Int
        ) {
            val stats = calculateStatistics(pokemon)
            with(writeInto) {
                // update stats
                writeBidEndianShort(dataOffset + 0x24, stats.health.toShort())
                writeBidEndianShort(dataOffset + 0x26, stats.attack.toShort())
                writeBidEndianShort(dataOffset + 0x28, stats.defense.toShort())
                writeBidEndianShort(dataOffset + 0x2A, stats.speed.toShort())
                writeBidEndianShort(dataOffset + 0x2C, stats.specialAttack.toShort())
                writeBidEndianShort(dataOffset + 0x2E, stats.specialDefense.toShort())
            }
        }
    }
}