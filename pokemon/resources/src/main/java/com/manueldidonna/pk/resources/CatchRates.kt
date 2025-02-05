package com.manueldidonna.pk.resources

import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.core.generation

fun getCatchRate(speciesId: Int, version: Version): Int {
    require(version.generation == 1) { "Only first generation games are supported" }
    require(speciesId in 1..151) { "Species ID not supported: $speciesId" }
    if (speciesId == 25) return 0xA3 // Light Ball Pikachu
    return CatchRates[speciesId - 1]
}

private val CatchRates = intArrayOf(
    45, 45, 45, 45, 45, 45, 45, 45, 45, 255, 120, 45, 255, 120, 45,
    255, 120, 45, 255, 90, 255, 90, 255, 90, 190, 75, 255, 90, 235, 120,
    45, 235, 120, 45, 150, 25, 190, 75, 170, 50, 255, 90, 255, 120, 45,
    190, 75, 190, 75, 255, 50, 255, 90, 190, 75, 190, 75, 190, 75, 255,
    120, 45, 200, 100, 50, 50, 180, 90, 45, 255, 120, 45, 190, 60, 255, 120,
    45, 190, 60, 190, 75, 190, 60, 45, 190, 45, 190, 75, 190, 75, 190,
    60, 190, 90, 45, 45, 190, 75, 225, 60, 190, 60, 90, 45, 190, 75,
    45, 45, 45, 190, 60, 120, 60, 30, 45, 45, 225, 75, 225, 60, 225,
    60, 45, 45, 45, 45, 45, 45, 45, 255, 45, 45, 35, 45, 45, 45,
    45, 45, 45, 45, 45, 45, 45, 25, 3, 3, 3, 45, 45, 45, 3,
    45
)
