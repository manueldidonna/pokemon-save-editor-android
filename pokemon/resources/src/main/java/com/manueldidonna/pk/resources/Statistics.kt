package com.manueldidonna.pk.resources

import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.Version
import kotlin.math.min
import kotlin.math.sqrt

private typealias Stats = Pokemon.StatisticValues

fun calculateStatistics(
    level: Int,
    base: Stats,
    individuals: Stats,
    efforts: Stats,
    version: Version
): Stats {
    require(version is Version.FirstGeneration) {
        "Only first generation games are supported"
    }

    fun firstGenerationFormula(base: Int, iv: Int, ev: Int): Int {
        val fixedEv = min(255.toDouble(), sqrt(ev.toDouble()) + 1).toInt() ushr 2
        return (((2 * (base + iv)) + fixedEv) * level / 100) + 5
    }

    val calculate: (base: Int, iv: Int, ev: Int) -> Int = ::firstGenerationFormula

    return object : Stats {
        override val health = calculate(base.health, individuals.health, efforts.health) + 5 + level
        override val attack = calculate(base.attack, individuals.attack, efforts.attack)
        override val defense = calculate(base.defense, individuals.defense, efforts.defense)
        override val speed = calculate(base.speed, individuals.speed, efforts.speed)
        override val specialAttack =
            calculate(base.specialAttack, individuals.specialAttack, efforts.specialAttack)
        override val specialDefense =
            calculate(base.specialDefense, individuals.specialDefense, efforts.specialDefense)
    }
}

fun getBaseStatistics(speciesId: Int, version: Version): Stats {
    require(version is Version.FirstGeneration) {
        "Only first generation games are supported"
    }
    require(speciesId in 1..151) {
        "Species ID not supported: $speciesId"
    }

    val index = (speciesId - 1) * 5
    return object : Stats {
        override val health = FirstGenBaseStatistics[index]
        override val attack = FirstGenBaseStatistics[index + 1]
        override val defense = FirstGenBaseStatistics[index + 2]
        override val speed: Int = FirstGenBaseStatistics[index + 3]
        override val specialAttack = FirstGenBaseStatistics[index + 4]
        override val specialDefense = specialAttack
    }
}

/**
 * HP - ATK - DEF - SPD - SPC
 *
 * TODO: move SPC values to a separate array and rename this to BaseStatistics
 *   adding sp_attack and sp_defense
 */
private val FirstGenBaseStatistics = intArrayOf(
    45, 49, 49, 45, 65, // 1 Bulbasaur
    60, 62, 63, 60, 80, // 2 Ivysaur
    80, 82, 83, 80, 100, // 3 Venusaur
    39, 52, 43, 65, 50, // 4 Charmander
    58, 64, 58, 80, 65, // 5 Charmeleon
    78, 84, 78, 100, 85, // 6 Charizard
    44, 48, 65, 43, 50, // 7 Squirtle
    59, 63, 80, 58, 65, // 9 Wartortle
    79, 83, 100, 78, 85, // 9 Blastoise
    45, 30, 35, 45, 20, // 10 Caterpie
    50, 20, 55, 30, 25, // 11 Metapod
    60, 45, 50, 70, 80, // 12 Butterfree
    40, 35, 30, 50, 20, // 13 Weedle
    45, 25, 50, 35, 25, // 14 Kakuna
    65, 80, 40, 75, 45, // 15 Beedrill
    40, 45, 40, 56, 35,
    63, 60, 55, 71, 50,
    83, 80, 75, 91, 70,
    30, 56, 35, 72, 25,
    55, 81, 60, 97, 50,
    40, 60, 30, 70, 31,
    65, 90, 65, 100, 61,
    35, 60, 44, 55, 40, // 23 Ekans
    60, 85, 69, 80, 65,
    35, 55, 30, 90, 50,
    60, 90, 55, 100, 90,
    50, 75, 85, 40, 30,
    75, 100, 110, 65, 55,
    55, 47, 52, 41, 40,
    70, 62, 67, 56, 55,
    90, 82, 87, 76, 75,
    46, 57, 40, 50, 40,
    61, 72, 57, 65, 55,
    81, 92, 77, 85, 75, // 34 Nidoking

    90, 85, 100, 85, 125, // 144 Articuno
    90, 90, 85, 100, 125, // 145 Zapdos
    90, 100, 90, 90, 125, // 146 Moltres
    41, 64, 45, 50, 50, // 147 Dratini
    61, 84, 65, 70, 70, // 148 Dragonair
    91, 134, 95, 80, 100, // 149 Dragonite
    106, 110, 90, 130, 154, // !50 Mewtwo
    100, 100, 100, 100, 100 // 151 Mew
)