package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokerus
import com.manueldidonna.pk.core.Trainer
import com.manueldidonna.pk.rby.converter.getGameBoySpecies
import com.manueldidonna.pk.rby.info.getFirstType
import com.manueldidonna.pk.rby.info.getSecondType
import com.manueldidonna.pk.rby.info.isEvolutionOf
import com.manueldidonna.pk.resources.*
import com.manueldidonna.pk.utils.getGameBoyDataFromString
import com.manueldidonna.pk.utils.readBigEndianUShort
import com.manueldidonna.pk.utils.writeBidEndianInt
import com.manueldidonna.pk.utils.writeBidEndianShort
import com.manueldidonna.pk.core.Pokemon as CorePokemon

internal class Mutator(
    private val pokemon: MutablePokemon,
    private val data: UByteArray,
) : MutablePokemon.Mutator {
    override fun speciesId(value: Int): MutablePokemon.Mutator = apply {
        require(value in 1..151) { "Unsupported species id: $value" }
        // set species id
        data[0] = getGameBoySpecies(value).toUByte()
        // set sane status
        data[0x4] = 0u
        // set cache rate
        if (!value.isEvolutionOf(pokemon.speciesId)) {
            // TODO: check if the pokemon is catchable in the game
            data[0x7] = getCatchRate(value, pokemon.version).toUByte()
        }
        // set types
        data[0x5] = getFirstType(value).toUByte()
        data[0x6] = getSecondType(value).toUByte()
        // set max health
        val base = getBaseStatistics(pokemon.speciesId, pokemon.version)
        val health = with(pokemon) {
            calculateStatistics(level, base, iV, eV, version).health
        }
        data.writeBidEndianShort(0x1, health.toShort())
    }

    override fun nickname(value: String, ignoreCase: Boolean): MutablePokemon.Mutator = apply {
        getGameBoyDataFromString(value, 10, false, 11, ignoreCase).copyInto(data, 0x2C)
    }

    override fun trainer(value: Trainer, ignoreNameCase: Boolean): MutablePokemon.Mutator {
        data.writeBidEndianShort(0xC, value.visibleId.coerceAtMost(65535).toShort())
        getGameBoyDataFromString(value.name, 7, false, 11, ignoreNameCase).copyInto(data, 0x21)
        return this
    }

    override fun experiencePoints(value: Int): MutablePokemon.Mutator = apply {
        val experienceGroup = getExperienceGroup(pokemon.speciesId)
        val coercedValue = value.coerceAtMost(getExperiencePoints(100, experienceGroup))
        data.writeBidEndianInt(0xE, coercedValue shl 8, write3Bytes = true)
        val newLevel = getLevel(coercedValue, experienceGroup)
        if (newLevel != pokemon.level) {
            level(newLevel)
        }
    }

    override fun level(value: Int): MutablePokemon.Mutator = apply {
        require(value in 1..100) { "Level $value is out of bounds [1 - 100]" }
        data[0x3] = value.toUByte()
        val sanitizedExperience = sanitizeExperiencePoints(
            points = pokemon.experiencePoints,
            level = value,
            experienceGroup = getExperienceGroup(pokemon.speciesId)
        )
        if (sanitizedExperience != pokemon.experiencePoints) {
            experiencePoints(sanitizedExperience)
        }
    }

    override fun move(index: Int, move: CorePokemon.Move): MutablePokemon.Mutator = apply {
        require(index in 0..3) { "Index $index is out of bounds [0 - 3]" }
        // set id
        data[0x08 + index] = move.id.toUByte()
        // set ups
        val ups = move.ups.coerceIn(0, 3)
        val upsIndex = 0X1D + index
        data[upsIndex] = (data[upsIndex] and 0x3Fu) or ((ups and 0x3) shl 6).toUByte()
        // set power points
        val pp = move.powerPoints.coerceIn(0, getPowerPoints(move.id, ups, pokemon.version))
        val ppIndex = 0X1D + index
        data[ppIndex] = (data[ppIndex] and 0xC0u) or pp.toUByte()
    }

    override fun shiny(value: Boolean): MutablePokemon.Mutator = apply {
        if (pokemon.isShiny == value) return@apply
        if (value) {
            individualValues(
                CorePokemon.StatisticValues.Immutable(
                    health = 10,
                    defense = 10,
                    specialAttack = 10,
                    speed = 10,
                    attack = pokemon.iV.attack or 2
                )
            )
        } else {
            individualValues(
                CorePokemon.StatisticValues.Immutable(
                    attack = Pokemon.NonShinyAttackValues.random()
                )
            )
        }
    }

    override fun individualValues(value: CorePokemon.StatisticValues): Mutator = apply {
        // health is ignored, in gen 1 it's determined by the other ivs
        var totalIVs = data.readBigEndianUShort(0x1b).toInt()
        fun setValue(value: Int, shiftAmount: Int) {
            if (value >= 0) {
                totalIVs = (totalIVs and (0xF shl shiftAmount).inv()) or
                        (value.coerceAtMost(0xF) shl shiftAmount)
            }
        }
        with(value) {
            setValue(attack, shiftAmount = 12)
            setValue(defense, shiftAmount = 8)
            setValue(speed, shiftAmount = 4)
            setValue(specialAttack, shiftAmount = 0)
            setValue(specialDefense, shiftAmount = 0)
        }
        data.writeBidEndianShort(0x1B, totalIVs.toShort())
    }

    override fun effortValues(value: CorePokemon.StatisticValues): Mutator = apply {
        fun setValue(value: Int, effortOffset: Int) {
            if (value >= 0) {
                data.writeBidEndianShort(effortOffset, value.coerceAtMost(65535).toShort())
            }
        }
        with(value) {
            setValue(health, effortOffset = 0x11)
            setValue(attack, effortOffset = 0x13)
            setValue(defense, effortOffset = 0x15)
            setValue(speed, effortOffset = 0x17)
            setValue(specialAttack, effortOffset = 0x19)
            setValue(specialDefense, effortOffset = 0x19)
        }
    }

    override fun form(value: CorePokemon.Form): Mutator = this
    override fun friendship(value: Int): Mutator = this
    override fun heldItemId(value: Int): Mutator = this
    override fun pokerus(value: Pokerus): Mutator = this
    override fun caughtData(value: CorePokemon.CaughtData): Mutator = this
}
