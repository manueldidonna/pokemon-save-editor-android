package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.core.Pokemon.CaughtData
import com.manueldidonna.pk.core.Pokemon.StatisticValues
import com.manueldidonna.pk.gsc.converter.getLocalItemId
import com.manueldidonna.pk.gsc.converter.getUnownLetterFromStatistics
import com.manueldidonna.pk.resources.*
import com.manueldidonna.pk.utils.getGameBoyDataFromString
import com.manueldidonna.pk.utils.readBigEndianUShort
import com.manueldidonna.pk.utils.writeBidEndianInt
import com.manueldidonna.pk.utils.writeBidEndianShort
import kotlin.random.Random
import com.manueldidonna.pk.core.Pokemon as CorePokemon

internal class Mutator(
    private val pokemon: MutablePokemon,
    private val data: UByteArray,
) : MutablePokemon.Mutator {

    override fun speciesId(value: Int): Mutator = apply {
        require(value in 1..251) { "Unsupported species id: $value" }
        data[0] = value.toUByte()
    }

    override fun nickname(value: String, ignoreCase: Boolean): Mutator = apply {
        getGameBoyDataFromString(value, 10, false, 11, ignoreCase).copyInto(data, 0x2B)
    }

    override fun trainer(value: Trainer, ignoreNameCase: Boolean): Mutator = apply {
        data.writeBidEndianShort(0x06, value.visibleId.coerceAtMost(65535).toShort())
        getGameBoyDataFromString(value.name, 7, false, 11, ignoreNameCase).copyInto(data, 0x20)
        // set trainer gender in pokemon crystal
        if (pokemon.version == Version.Crystal) {
            var caughtData = data.readBigEndianUShort(0x1D).toInt()
            val genderValue = if (value.gender == Trainer.Gender.Male) 0 else 1
            caughtData = (caughtData and 0xFF7F) or ((genderValue and 1) shl 7)
            data.writeBidEndianShort(0x1D, caughtData.toShort())
        }
    }

    override fun experiencePoints(value: Int): Mutator = apply {
        val experienceGroup = getExperienceGroup(pokemon.speciesId)
        val coercedValue = value.coerceAtMost(getExperiencePoints(100, experienceGroup))
        data.writeBidEndianInt(0x08, coercedValue shl 8, write3Bytes = true)
        val newLevel = getLevel(experience = coercedValue, group = experienceGroup)
        if (newLevel != pokemon.level) {
            level(newLevel)
        }
    }

    override fun level(value: Int): Mutator = apply {
        require(value in 1..100) { "Level $value is out of bounds [1..100]" }
        data[0x1F] = value.toUByte()
        val sanitizedExperience = sanitizeExperiencePoints(
            speciesId = pokemon.speciesId,
            experience = pokemon.experiencePoints,
            level = value
        )
        if (sanitizedExperience != pokemon.experiencePoints) {
            experiencePoints(sanitizedExperience)
        }
    }

    override fun move(index: Int, move: CorePokemon.Move): Mutator = apply {
        require(index in 0..3) { "Index $index is out of bounds [0..3]" }
        // set id
        data[0x02 + index] = move.id.toUByte()
        val ppIndex = 0x17 + index
        // set ups
        val ups = move.ups.coerceIn(0, 3)
        data[ppIndex] = (data[ppIndex] and 0x3Fu) or ((ups and 0x3) shl 6).toUByte()
        // set power points
        val pp = move.powerPoints.coerceIn(0, getPowerPoints(move.id, ups, pokemon.version))
        data[ppIndex] = (data[ppIndex] and 0xC0u) or pp.toUByte()
    }

    override fun shiny(value: Boolean): Mutator = apply {
        if (pokemon.isShiny == value) return@apply
        if (value) {
            individualValues(
                StatisticValues.Immutable(
                    health = 10,
                    defense = 10,
                    specialAttack = 10,
                    speed = 10,
                    attack = pokemon.iV.attack or 2
                )
            )
        } else {
            individualValues(StatisticValues.Immutable(attack = NonShinyAttackValues.random()))
        }
    }

    override fun friendship(value: Int): Mutator = apply {
        require(value in 0..255) { "Value $value is out of bounds [0-225]" }
        data[0x1B] = value.toUByte()
    }

    override fun heldItemId(value: Int): Mutator = apply {
        // TODO: validate value
        data[0x1] = getLocalItemId(value).toUByte()
    }

    override fun pokerus(value: Pokerus): Mutator = apply {
        var pkrs = 0
        pkrs = (pkrs and 0xF) or value.strain shl 4
        pkrs = (pkrs and 0xF.inv()) or value.days
        data[0x1C] = pkrs.toUByte()
    }

    override fun individualValues(value: StatisticValues): Mutator = apply {
        // health is ignored, in gen 2 it's determined by the other ivs
        var ivs = data.readBigEndianUShort(0x15).toInt()
        fun setValue(value: Int, shift: Int): Int {
            return if (value < 0) ivs
            else (ivs and (0xF shl shift).inv()) or (value.coerceAtMost(0xF) shl shift)
        }
        with(value) {
            ivs = setValue(attack, shift = 12)
            ivs = setValue(defense, shift = 8)
            ivs = setValue(speed, shift = 4)
            ivs = setValue(specialAttack, shift = 0)
            ivs = setValue(specialDefense, shift = 0)
        }
        data.writeBidEndianShort(0x15, ivs.toShort())
    }

    override fun effortValues(value: StatisticValues): Mutator = apply {
        fun setValue(value: Int, effortOffset: Int) {
            if (value < 0) return
            data.writeBidEndianShort(effortOffset, value.coerceAtMost(65535).toShort())
        }
        with(value) {
            setValue(health, effortOffset = 0x0B)
            setValue(attack, effortOffset = 0x0D)
            setValue(defense, effortOffset = 0x0F)
            setValue(speed, effortOffset = 0x11)
            setValue(specialAttack, effortOffset = 0x13)
            setValue(specialDefense, effortOffset = 0x13)
        }
    }

    override fun form(value: CorePokemon.Form): Mutator = apply {
        require(value is CorePokemon.Form.Unown) { "Unexpected form $value" }
        require(pokemon.speciesId == 201) { "Pokemon must be an Unown" }
        require(isValidUnownLetter(value.letter, pokemon.version))
        val letter = value.letter.toUpperCase()
        val mutableStats = object : StatisticValues {
            override var health: Int = -1
            override var attack: Int = -1
            override var defense: Int = -1
            override var specialAttack: Int = -1
            override var specialDefense: Int = -1
            override var speed: Int = -1
        }
        while (getUnownLetterFromStatistics(pokemon.iV) != letter) {
            individualValues(mutableStats.apply {
                health = Random.nextInt(until = 16)
                attack = Random.nextInt(until = 16)
                defense = Random.nextInt(until = 16)
                speed = Random.nextInt(until = 16)
                specialAttack = Random.nextInt(until = 16)
                specialDefense = Random.nextInt(until = 16)
            })
        }
    }

    override fun caughtData(value: CaughtData): Mutator = apply {
        if (pokemon.version != Version.Crystal) return@apply
        val (metLevel, time, locationId) = value

        require(time is CaughtData.Time.TimeOfDay) { "Invalid time format: $time" }
        require(metLevel in 0..100) { "Met level is out of bounds [0..100]" }
        require(isLocationIdValid(locationId, pokemon.version)) {
            "Invalid met location id: $locationId"
        }

        val timeValue = when (time) {
            CaughtData.Time.TimeOfDay.Morning -> 1
            CaughtData.Time.TimeOfDay.DayTime -> 2
            CaughtData.Time.TimeOfDay.Night -> 3
        }

        var caught = data.readBigEndianUShort(0x1D).toInt()
        caught = (caught and 0xC0FF) or (metLevel and 0x3F shl 8)
        caught = (caught and 0xFF80) or (locationId and 0x7F)
        caught = (caught and 0x3FFF) or (timeValue and 0x3 shl 14)
        data.writeBidEndianShort(0x1D, caught.toShort())
    }

    companion object {
        private val NonShinyAttackValues = listOf(1, 4, 5, 8, 9, 12, 13)
    }
}
