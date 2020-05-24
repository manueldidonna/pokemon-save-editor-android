package com.manueldidonna.pk.resources

import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.Version
import kotlin.math.min
import kotlin.math.sqrt

private typealias Stats = Pokemon.StatisticValues

private typealias StatisticsFormula = (level: Int, base: Int, iv: Int, ev: Int) -> Int

private val GameBoyDefaultFormula: StatisticsFormula = { level, base, iv, ev ->
    val fixedEv = min(255.toDouble(), sqrt(ev.toDouble()) + 1).toInt() ushr 2
    (2 * (base + iv) + fixedEv) * level / 100 + 5
}

private val GameBoyHealthFormula: StatisticsFormula = { level, base, iv, ev ->
    GameBoyDefaultFormula(level, base, iv, ev) + 5 + level
}


fun calculateStatistics(level: Int, base: Stats, ivs: Stats, evs: Stats, version: Version): Stats {
    require(version is Version.FirstGeneration) {
        "Only first generation games are supported"
    }
    val stat: StatisticsFormula = GameBoyDefaultFormula
    val health: StatisticsFormula = GameBoyHealthFormula
    return object : Stats {
        override val health = health(level, base.health, ivs.health, evs.health)
        override val attack = stat(level, base.attack, ivs.attack, evs.attack)
        override val defense = stat(level, base.defense, ivs.defense, evs.defense)
        override val speed = stat(level, base.speed, ivs.speed, evs.speed)
        override val specialAttack =
            stat(level, base.specialAttack, ivs.specialAttack, evs.specialAttack)
        override val specialDefense =
            stat(level, base.specialDefense, ivs.specialDefense, evs.specialDefense)
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
        override val speed = FirstGenBaseStatistics[index + 3]
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
    70, 45, 48, 35, 60,
    95, 70, 73, 60, 85,
    38, 41, 40, 65, 65,
    73, 76, 75, 100, 100,
    115, 45, 20, 20, 25,
    140, 70, 45, 45, 50,
    40, 45, 35, 55, 40,
    75, 80, 70, 90, 75,
    45, 50, 55, 30, 75,
    60, 65, 70, 40, 85,
    75, 80, 85, 50, 100,
    35, 70, 55, 25, 55,
    60, 95, 80, 30, 80,
    60, 55, 50, 45, 40,
    70, 65, 60, 90, 90,
    10, 55, 25, 95, 45,
    35, 80, 50, 120, 70,
    40, 45, 35, 90, 40,
    65, 70, 60, 115, 65,
    50, 52, 48, 55, 50,
    80, 82, 78, 85, 80, // 55 Golduck
    40, 80, 35, 70, 35, // 56 Mankey
    65, 105, 60, 95, 60,
    55, 70, 45, 60, 50,
    90, 110, 80, 95, 80,
    40, 50, 40, 90, 40,
    65, 65, 65, 90, 50,
    90, 85, 95, 70, 70,
    25, 20, 15, 90, 105,
    40, 35, 30, 105, 120,
    55, 50, 45, 120, 135,
    70, 80, 50, 35, 35,
    80, 100, 70, 45, 50,
    90, 130, 80, 55, 65,
    50, 75, 35, 40, 70,
    65, 90, 50, 55, 85,
    80, 105, 65, 70, 100,
    40, 40, 35, 70, 100,
    80, 70, 65, 100, 120,
    40, 80, 100, 20, 30,
    55, 95, 115, 35, 45,
    80, 110, 130, 45, 55,
    50, 85, 55, 90, 65,
    65, 100, 70, 105, 80,
    90, 65, 65, 15, 40,
    95, 75, 110, 30, 80,
    25, 35, 70, 45, 95,
    50, 60, 95, 70, 120,
    52, 65, 55, 60, 58,
    35, 85, 45, 75, 35,
    60, 110, 70, 100, 60,
    65, 45, 55, 45, 70,
    90, 70, 80, 70, 95,
    80, 80, 50, 25, 40,
    105, 105, 75, 50, 65,
    30, 65, 100, 40, 45,
    50, 95, 180, 70, 85,
    30, 35, 30, 80, 100,
    45, 50, 45, 95, 115,
    60, 65, 60, 110, 130,
    35, 45, 160, 70, 30,
    60, 48, 45, 42, 90,
    85, 73, 70, 67, 115,
    30, 105, 90, 50, 25,
    55, 130, 115, 75, 50,
    40, 30, 50, 100, 55,
    60, 50, 70, 140, 80,
    60, 40, 80, 40, 60,
    95, 95, 85, 55, 125,
    50, 50, 95, 35, 40,
    60, 80, 110, 45, 50,
    50, 120, 53, 87, 35,
    50, 105, 79, 76, 35,
    90, 55, 75, 30, 60,
    40, 65, 95, 35, 60,
    65, 90, 120, 60, 85,
    80, 85, 95, 25, 30,
    105, 130, 120, 40, 45,
    250, 5, 5, 50, 105,
    65, 55, 115, 60, 100,
    105, 95, 80, 90, 40,
    30, 40, 70, 60, 70,
    55, 65, 95, 85, 95,
    45, 67, 60, 63, 50,
    80, 92, 65, 68, 80,
    30, 45, 55, 85, 70,
    60, 75, 85, 115, 100,
    40, 45, 65, 90, 100,
    70, 110, 80, 105, 55,
    65, 50, 35, 95, 95,
    65, 83, 57, 105, 85,
    65, 95, 57, 93, 85,
    65, 125, 100, 85, 55,
    75, 100, 95, 110, 70,
    20, 10, 55, 80, 20,
    95, 125, 79, 81, 100,
    130, 85, 80, 60, 95,
    48, 48, 48, 48, 48,
    55, 55, 50, 55, 65,
    130, 65, 60, 65, 110,
    65, 65, 60, 130, 110,
    65, 130, 60, 65, 110,
    65, 60, 70, 40, 75,
    35, 40, 100, 35, 90,
    70, 60, 125, 55, 115,
    30, 80, 90, 55, 45,
    60, 115, 105, 80, 70,
    80, 105, 65, 130, 60,
    160, 110, 65, 30, 65,
    90, 85, 100, 85, 125, // 144 Articuno
    90, 90, 85, 100, 125, // 145 Zapdos
    90, 100, 90, 90, 125, // 146 Moltres
    41, 64, 45, 50, 50, // 147 Dratini
    61, 84, 65, 70, 70, // 148 Dragonair
    91, 134, 95, 80, 100, // 149 Dragonite
    106, 110, 90, 130, 154, // !50 Mewtwo
    100, 100, 100, 100, 100 // 151 Mew
)