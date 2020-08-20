package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.gsc.Pokemon.Companion.getUnownLetter
import com.manueldidonna.pk.gsc.converter.getLocalItemId
import com.manueldidonna.pk.resources.*
import com.manueldidonna.pk.utils.getGameBoyDataFromString
import com.manueldidonna.pk.utils.readBigEndianUShort
import com.manueldidonna.pk.utils.writeBidEndianInt
import com.manueldidonna.pk.utils.writeBidEndianShort
import kotlin.random.Random

internal class Mutator(
    private val pokemon: MutablePokemon,
    private val data: UByteArray
) : MutablePokemon.Mutator {

    override fun speciesId(value: Int): MutablePokemon.Mutator = apply {
        require(value in 1..251) { "Not supported species id: $value" }
        data[0] = value.toUByte()
    }

    override fun nickname(value: String, ignoreCase: Boolean): MutablePokemon.Mutator = apply {
        getGameBoyDataFromString(value, 10, false, 11, ignoreCase).copyInto(data, 0x2B)
    }

    override fun trainer(value: Trainer, ignoreNameCase: Boolean): MutablePokemon.Mutator = apply {
        data.writeBidEndianShort(0x06, value.visibleId.coerceAtMost(65535).toShort())
        getGameBoyDataFromString(value.name, 7, false, 11, ignoreNameCase).copyInto(data, 0x20)
        // set trainer gender in pokemon crystal
        if (pokemon.version == Version.Crystal && pokemon.trainer.gender != value.gender) {
            var caughtData = data.readBigEndianUShort(0x1D).toInt()
            val genderValue = if (value.gender == Trainer.Gender.Male) 0 else 1
            caughtData = (caughtData and 0xFF7F) or ((genderValue and 1) shl 7)
            data.writeBidEndianShort(0x1D, caughtData.toShort())
        }
    }

    override fun experiencePoints(value: Int): MutablePokemon.Mutator = apply {
        val experienceGroup = getExperienceGroup(pokemon.speciesId)
        val coercedValue = value.coerceAtMost(getExperiencePoints(100, experienceGroup))
        data.writeBidEndianInt(0x08, coercedValue shl 8, write3Bytes = true)
        val newLevel = getLevel(experience = coercedValue, group = experienceGroup)
        if (newLevel != pokemon.level) {
            level(newLevel)
        }
    }

    override fun level(value: Int): MutablePokemon.Mutator = apply {
        require(value in 1..100) { "Level $value is out of bounds [1 - 100]" }
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

    override fun move(index: Int, move: Pokemon.Move): MutablePokemon.Mutator = apply {
        require(index in 0..3) { "Index $index is out of bounds [0 - 3]" }
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

    override fun shiny(value: Boolean): MutablePokemon.Mutator = apply {
        if (pokemon.isShiny == value) return@apply
        if (value) {
            individualValues(
                health = 10,
                defense = 10,
                specialAttack = 10,
                speed = 10,
                attack = pokemon.iV.attack or 2
            )
        } else {
            individualValues(attack = NonShinyAttackValues.random())
        }
    }

    override fun friendship(value: Int): MutablePokemon.Mutator = apply {
        require(value in 0..255) { "Value $value is out of bounds [0-225]" }
        data[0x1B] = value.toUByte()
    }

    override fun heldItemId(value: Int): MutablePokemon.Mutator = apply {
        data[0x1] = getLocalItemId(value).toUByte()
    }

    override fun pokerus(value: Pokerus): MutablePokemon.Mutator = apply {
        var pkrs = 0
        // set strain
        pkrs = (pkrs and 0xF) or value.strain shl 4
        pkrs = (pkrs and 0xF.inv()) or value.days
        data[0x1C] = pkrs.toUByte()
    }

    override fun individualValues(
        health: Int,
        attack: Int,
        defense: Int,
        speed: Int,
        specialAttack: Int,
        specialDefense: Int
    ): MutablePokemon.Mutator = apply {
        // health is ignored, in gen 2 it's determined by the other ivs
        var ivs = data.readBigEndianUShort(0x15).toInt()
        fun setValue(value: Int, shift: Int): Int {
            return if (value < 0) ivs
            else (ivs and (0xF shl shift).inv()) or (value.coerceAtMost(0xF) shl shift)
        }
        ivs = setValue(attack, shift = 12)
        ivs = setValue(defense, shift = 8)
        ivs = setValue(speed, shift = 4)
        ivs = setValue(specialAttack, shift = 0)
        ivs = setValue(specialDefense, shift = 0)
        data.writeBidEndianShort(0x15, ivs.toShort())
    }

    override fun effortValues(
        health: Int,
        attack: Int,
        defense: Int,
        speed: Int,
        specialAttack: Int,
        specialDefense: Int
    ): MutablePokemon.Mutator = apply {
        fun setValue(value: Int, effortOffset: Int) {
            if (value < 0) return
            data.writeBidEndianShort(effortOffset, value.coerceAtMost(65535).toShort())
        }
        setValue(health, effortOffset = 0x0B)
        setValue(attack, effortOffset = 0x0D)
        setValue(defense, effortOffset = 0x0F)
        setValue(speed, effortOffset = 0x11)
        setValue(specialAttack, effortOffset = 0x13)
        setValue(specialDefense, effortOffset = 0x13)
    }

    override fun form(value: Pokemon.Form): MutablePokemon.Mutator = apply {
        require(value is Pokemon.Form.Unown) { "Unexpected form $value" }
        require(pokemon.speciesId == 201) { "Pokemon must be an Unown" }
        val letter = value.letter.toUpperCase()
        require(isValidUnownLetter(letter, pokemon.version))
        while (getUnownLetter(pokemon) != letter) {
            individualValues(
                health = Random.nextInt(until = 16),
                attack = Random.nextInt(until = 16),
                defense = Random.nextInt(until = 16),
                speed = Random.nextInt(until = 16),
                specialAttack = Random.nextInt(until = 16),
                specialDefense = Random.nextInt(until = 16)
            )
        }
    }

    override fun metInfo(value: MetInfo): MutablePokemon.Mutator = apply {
        val (metLevel, time, locationId, gender) = value
        require(time is MetInfo.Time.TimesOfDay && time.value in 1..3) {
            "Invalid time format or value for $time"
        }
        require(metLevel in 0..100) {
            "Met level is out of bounds [0-100]"
        }
        require(isLocationIdValid(locationId, pokemon.version)) {
            "Invalid met location. Id is : $locationId"
        }

        var caught = data.readBigEndianUShort(0x1D).toInt()
        caught = (caught and 0xC0FF) or (metLevel and 0x3F shl 8)
        caught = (caught and 0xFF80) or (locationId and 0x7F)
        caught = (caught and 0x3FFF) or (time.value and 0x3 shl 14)
        val genderValue = if (gender == Trainer.Gender.Male) 0 else 1
        caught = (caught and 0xFF7F) or ((genderValue and 1) shl 7)
        data.writeBidEndianShort(0x1D, caught.toShort())
    }

    companion object {
        private val NonShinyAttackValues = listOf(1, 4, 5, 8, 9, 12, 13)
    }
}
