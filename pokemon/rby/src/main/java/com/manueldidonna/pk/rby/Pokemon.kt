package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.rby.Pokemon.Mutator
import com.manueldidonna.pk.rby.converter.getGameBoyDataFromString
import com.manueldidonna.pk.rby.converter.getGameBoySpecies
import com.manueldidonna.pk.rby.converter.getNationalSpecies
import com.manueldidonna.pk.rby.converter.getStringFromGameBoyData
import com.manueldidonna.pk.rby.info.getFirstType
import com.manueldidonna.pk.rby.info.getSecondType
import com.manueldidonna.pk.rby.info.ifNull
import com.manueldidonna.pk.rby.info.isEvolutionOf
import com.manueldidonna.pk.rby.utils.readBigEndianInt
import com.manueldidonna.pk.rby.utils.readBigEndianUShort
import com.manueldidonna.pk.rby.utils.writeBidEndianInt
import com.manueldidonna.pk.rby.utils.writeBidEndianShort
import com.manueldidonna.pk.resources.*
import com.manueldidonna.pk.core.Pokemon as CorePokemon

/**
 * 0x00 0x1 - species ID
 * 0x01 0x2 - current HP
 * 0x03 0x1 - level
 * 0x04 0x1 - status condition (@see [Mutator.status] & [Pokemon.StatusToValue])
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
 *
 * Gen1 games store the names in a different location of the pokemon box
 * For a mutable instance the real offsets are [trainerNameOffset] & [pokemonNameOffset]
 *
 */
internal class Pokemon private constructor(
    private val data: UByteArray,
    private val speciesOffset: Int,
    private val startOffset: Int,
    private val trainerNameOffset: Int,
    private val pokemonNameOffset: Int,
    private val index: Int,
    slot: Int,
    override val version: Version
) : MutablePokemon {

    init {
        require(startOffset != 0 || data.size == FullDataSizeInBox) {
            "This instance is neither mutable or immutable"
        }
    }

    override fun asBytes(): UByteArray {
        if (data.size == FullDataSizeInBox)
            return data.copyOf()
        val (index, slot) = position
        return getPokemonData(data, startOffset, index, slot)
    }

    override val isEmpty: Boolean
        get() = speciesId == 0 || nickname.isEmpty()

    override val position by lazy { CorePokemon.Position(index, slot) }

    override val trainer: Trainer
        get() = Trainer(
            name = getStringFromGameBoyData(data, trainerNameOffset, 11, false),
            visibleId = data.readBigEndianUShort(startOffset + 0x0C).toInt(),
            secretId = 0 // unused in gen 1
        )

    override val speciesId: Int
        get() = getNationalSpecies(data[startOffset].toInt())

    override val nickname: String
        get() = getStringFromGameBoyData(data, pokemonNameOffset, 11, false)

    override val level: Int
        get() {
            return when {
                // fast path, box pokemon. Read from the level offset
                !index.isPartyIndex -> data[startOffset + 0x3].toInt()
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

    /**
     * This property han been introduced in the second gen. games. When this pokemon is transf
     */
    override val isShiny: Boolean
        get() {
            with(iV) {
                if (speed != 10) return false
                if (specialAttack != 10) return false
                if (defense != 10) return false
                return attack in ShinyAttackValues
            }
        }

    override fun <T> selectMove(index: Int, mapTo: (id: Int, powerPoints: Int, ups: Int) -> T): T {
        require(index in 0..3) { "Move index is out of bounds [0 - 3]" }
        return mapTo(
            data[startOffset + 0x08 + index].toInt(),
            data[startOffset + 0x1D + index].toInt() and 0x3F,
            (data[startOffset + 0x1D + index].toInt() and 0xC0) ushr 6
        )
    }

    /**
     * [CorePokemon.StatisticValues.specialDefense] is equal to [CorePokemon.StatisticValues.specialAttack]
     */
    override val iV: CorePokemon.StatisticValues by lazy {
        object : CorePokemon.StatisticValues {
            private val DV16: Int
                get() = data.readBigEndianUShort(startOffset + 0x1b).toInt()

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
                get() = data.readBigEndianUShort(startOffset + 0x11).toInt()

            override val attack: Int
                get() = data.readBigEndianUShort(startOffset + 0x13).toInt()

            override val defense: Int
                get() = data.readBigEndianUShort(startOffset + 0x15).toInt()

            override val speed: Int
                get() = data.readBigEndianUShort(startOffset + 0x17).toInt()

            override val specialAttack: Int
                get() = data.readBigEndianUShort(startOffset + 0x19).toInt()

            override val specialDefense: Int
                get() = specialAttack
        }
    }

    override val mutator: MutablePokemon.Mutator by lazy { Mutator() }

    inner class Mutator : MutablePokemon.Mutator {

        init {
            require(startOffset != 0) { "This Pokemon instance is read-only" }
        }

        private var baseStatistics: CorePokemon.StatisticValues? = null

        override fun speciesId(value: Int): MutablePokemon.Mutator = apply {
            require(value in 1..151) { "Not supported species id: $value" }
            // set species id
            data[speciesOffset] = getGameBoySpecies(value).toUByte()
            data[startOffset] = getGameBoySpecies(value).toUByte()
            // set sane status
            data[startOffset + 0x4] = 0u
            // set cache rate
            if (!value.isEvolutionOf(speciesId)) {
                // TODO: check if the pokemon is catchable in the game
                data[startOffset + 0x7] = getCatchRate(value, version).toUByte()
            }
            // set types
            val firstType = getFirstType(value)
            data[startOffset + 0x5] = firstType.value.toUByte()
            data[startOffset + 0x6] = getSecondType(value).ifNull(firstType).value.toUByte()
            // set max statistics per species
            baseStatistics = getBaseStatistics(value, version)
            maximizeStatistics()
        }

        override fun nickname(value: String, ignoreCase: Boolean): MutablePokemon.Mutator = apply {
            getGameBoyDataFromString(value, 10, false, 11, ignoreCase)
                .copyInto(data, pokemonNameOffset)
        }

        override fun trainer(value: Trainer, ignoreNameCase: Boolean): MutablePokemon.Mutator {
            data.writeBidEndianShort(
                startOffset + 0xC,
                value.visibleId.coerceAtMost(65535).toShort()
            )
            getGameBoyDataFromString(value.name, 7, false, 11, ignoreNameCase)
                .copyInto(data, trainerNameOffset)
            return this
        }

        override fun experiencePoints(value: Int): MutablePokemon.Mutator = apply {
            val experienceGroup = getExperienceGroup(speciesId)
            val coercedValue = value.coerceAtMost(getExperiencePoints(100, experienceGroup))
            data.writeBidEndianInt(startOffset + 0xE, coercedValue shl 8, write3Bytes = true)
            val newLevel = getLevel(coercedValue, experienceGroup)
            if (newLevel != level) {
                level(newLevel)
            }
        }

        override fun level(value: Int): MutablePokemon.Mutator = apply {
            val coercedLevel = value.coerceIn(1, 100)
            data[startOffset + 0x3] = coercedLevel.toUByte()
            if (index.isPartyIndex) {
                data[startOffset + 0x21] = coercedLevel.toUByte()
            }
            // TODO: remove statistics invocation
            maximizeStatistics()
            val sanitizedExperience = sanitizeExperiencePoints(
                points = experiencePoints,
                level = coercedLevel,
                experienceGroup = getExperienceGroup(speciesId)
            )
            if (sanitizedExperience != experiencePoints) {
                experiencePoints(sanitizedExperience)
            }
        }

        override fun move(index: Int, move: CorePokemon.Move): MutablePokemon.Mutator = apply {
            require(index in 0..3) { "Index $index is out of bounds [0 - 3]" }
            // set id
            data[startOffset + 0x08 + index] = move.id.toUByte()
            // set ups
            val ups = move.ups.coerceIn(0, 3)
            val upsIndex = startOffset + 0X1D + index
            data[upsIndex] = (data[upsIndex] and 0x3Fu) or ((ups and 0x3) shl 6).toUByte()
            // set power points
            val pp = move.powerPoints.coerceIn(0, getPowerPoints(move.id, ups))
            val ppIndex = startOffset + 0X1D + index
            data[ppIndex] = (data[ppIndex] and 0xC0u) or pp.toUByte()
        }

        override fun shiny(value: Boolean): MutablePokemon.Mutator = apply {
            if (isShiny == value) return@apply
            if (value) {
                individualValues(
                    health = 10,
                    defense = 10,
                    specialAttack = 10,
                    speed = 10,
                    attack = ShinyAttackValues.random()
                )
            } else {
                // TODO: find a better way
                individualValues(defense = 9)
            }
        }

        /**
         * [specialAttack] and [specialDefense] represents the same attribute that is 'special'
         * If both are greater than -1, [specialDefense] will override [specialAttack]
         */
        override fun individualValues(
            health: Int,
            attack: Int,
            defense: Int,
            speed: Int,
            specialAttack: Int,
            specialDefense: Int
        ): MutablePokemon.Mutator = apply {
            // health is ignored, in gen 1 it's determined by the other ivs
            var totalIVs = data.readBigEndianUShort(startOffset + 0x1b).toInt()
            fun setValue(value: Int, shiftAmount: Int) {
                if (value >= 0) {
                    totalIVs = (totalIVs and (0xF shl shiftAmount).inv()) or
                            (value.coerceAtMost(0xF) shl shiftAmount)
                }
            }
            setValue(attack, shiftAmount = 12)
            setValue(defense, shiftAmount = 8)
            setValue(speed, shiftAmount = 4)
            setValue(specialAttack, shiftAmount = 0)
            setValue(specialDefense, shiftAmount = 0)
            data.writeBidEndianShort(startOffset + 0x1B, totalIVs.toShort())
            // TODO: remove statistics invocation
            maximizeStatistics()
        }

        /**
         * [specialAttack] and [specialDefense] represents the same attribute that is 'special'
         * If both are greater than -1, [specialDefense] will override [specialAttack]
         */
        override fun effortValues(
            health: Int,
            attack: Int,
            defense: Int,
            speed: Int,
            specialAttack: Int,
            specialDefense: Int
        ): MutablePokemon.Mutator = apply {
            fun setValue(value: Int, effortOffset: Int) {
                if (value >= 0) {
                    data.writeBidEndianShort(
                        startOffset + effortOffset,
                        value.coerceAtMost(65535).toShort()
                    )
                }
            }
            setValue(health, effortOffset = 0x11)
            setValue(attack, effortOffset = 0x13)
            setValue(defense, effortOffset = 0x15)
            setValue(speed, effortOffset = 0x17)
            setValue(specialAttack, effortOffset = 0x19)
            setValue(specialDefense, effortOffset = 0x19)
            // TODO: remove statistics invocation
            maximizeStatistics()
        }

        private fun maximizeStatistics() {
            fun setStat(offset: Int, value: Int) {
                data.writeBidEndianShort(startOffset + offset, value.toShort())
            }

            val baseStatistics = baseStatistics
                ?: getBaseStatistics(speciesId, version).also { baseStatistics = it }

            val stats = calculateStatistics(level, baseStatistics, iV, eV, version)

            // current HP
            setStat(offset = 0x1, value = stats.health)

            if (index.isPartyIndex) {
                setStat(offset = 0x22, value = stats.health)
                setStat(offset = 0x24, value = stats.attack)
                setStat(offset = 0x26, value = stats.defense)
                setStat(offset = 0x28, value = stats.speed)
                setStat(offset = 0x2A, value = stats.specialAttack)
            }
        }
    }

    companion object {
        /**
         * Box Data + Trainer Name + Nickname
         */
        internal const val FullDataSizeInBox = 33 + 0xb * 2

        /**
         * Pokemon Data stored in the box, without trainer name and nickname
         */
        internal const val DataSizeInBox = 33

        /**
         * Box Data + Party data, without trainer name and nickname
         */
        internal const val DataSizeInParty = 44

        /**
         * Used by trainer name and pokemon nicknames
         */
        internal const val NameMaxSize = 11

        private val ShinyAttackValues = listOf(2, 3, 6, 7, 10, 11, 14, 15)

        val EmptyTemplate = object : MutablePokemon.Template {
            override val name = "Empty Pokemon"
            override val description = "An empty template for R/B/Y games"
            override val speciesId = 151
            override fun apply(pokemon: MutablePokemon) {
                pokemon.mutator
                    .speciesId(151)
                    .level(1)
                    .move(index = 0, move = CorePokemon.Move.maxPowerPoints(1))
                    .move(index = 1, move = CorePokemon.Move.Empty)
                    .move(index = 2, move = CorePokemon.Move.Empty)
                    .move(index = 3, move = CorePokemon.Move.Empty)
                    .nickname("TEMPLATE")
                    .trainer(Trainer("TRAINER", 12345, 0))
                    .individualValues(all = 15)
                    .effortValues(all = 999999)
            }
        }

        fun newMutableInstance(
            data: UByteArray,
            index: Int,
            slot: Int,
            version: Version,
            startOffset: Int
        ): MutablePokemon {
            val (dataOfs, trainerNameOfs, nickOfs) =
                Storage.getPokemonOffsets(index, startOffset, slot)
            return Pokemon(
                data = data,
                speciesOffset = startOffset + 1 + 1 * slot,
                startOffset = dataOfs,
                trainerNameOffset = trainerNameOfs,
                pokemonNameOffset = nickOfs,
                index = index,
                slot = slot,
                version = version
            )
        }

        fun newImmutableInstance(
            data: UByteArray,
            index: Int,
            slot: Int,
            version: Version,
            startOffset: Int
        ): Pokemon {
            return Pokemon(
                data = getPokemonData(data, startOffset, index, slot),
                speciesOffset = 0,
                startOffset = 0,
                trainerNameOffset = DataSizeInBox,
                pokemonNameOffset = DataSizeInBox + NameMaxSize,
                index = index,
                slot = slot,
                version = version
            )
        }

        private fun getPokemonData(
            data: UByteArray,
            startOffset: Int,
            index: Int,
            slot: Int
        ): UByteArray {
            val (dataOfs, trainerNameOfs, nickOfs) =
                Storage.getPokemonOffsets(index, startOffset, slot)
            return UByteArray(FullDataSizeInBox).apply {
                // Copy Pokemon Box Data
                data.copyInto(this, 0, dataOfs, dataOfs + DataSizeInBox)
                // Copy Trainer Name
                data.copyInto(this, DataSizeInBox, trainerNameOfs, trainerNameOfs + NameMaxSize)
                // Copy Pokemon Name
                data.copyInto(this, DataSizeInBox + NameMaxSize, nickOfs, nickOfs + NameMaxSize)
            }
        }
    }
}
