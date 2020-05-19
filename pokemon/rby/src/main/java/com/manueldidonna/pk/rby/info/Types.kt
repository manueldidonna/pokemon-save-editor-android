package com.manueldidonna.pk.rby.info

internal inline class PokemonType(val value: Int)

internal fun PokemonType.ifNull(otherValue: PokemonType): PokemonType {
    return if (this.value == -1) otherValue else this
}

internal fun getFirstType(speciesId: Int): PokemonType {
    return PokemonType(FirstTypes[speciesId - 1])
}

internal fun getSecondType(speciesId: Int): PokemonType {
    return PokemonType(SecondTypes[speciesId - 1])
}

private val FirstTypes = intArrayOf(
    3, 3, 3, 20, 20, 2, 21, 21, 21, 7, 7, 2, 3, 3, 3, 2, 2, 2, 0, 0, 2, 2, 3, 3, 23, 23, 4, 4,
    3, 3, 4, 3, 3, 4, 0, 0, 20, 20, 0, 0, 2, 2, 3, 3, 3, 22, 22, 3, 3, 4, 4, 0, 0, 21, 21, 1,
    1, 20, 20, 21, 21, 1, 24, 24, 24, 1, 1, 1, 3, 3, 3, 3, 3, 4, 4, 4, 20, 20, 24, 24, 23, 23,
    2, 2, 2, 21, 25, 3, 3, 21, 25, 3, 3, 3, 4, 24, 24, 21, 21, 23, 23, 24, 24, 4, 4, 1, 1, 0,
    3, 3, 5, 5, 0, 22, 0, 21, 21, 21, 21, 21, 24, 0, 2, 24, 23, 20, 7, 0, 21, 2, 25, 0, 0, 21,
    23, 20, 0, 21, 21, 21, 21, 2, 0, 2, 2, 2, 26, 26, 2, 24, 24
)

private val SecondTypes = intArrayOf(
    22, 22, 22, -1, -1, 20, -1, -1, -1, -1, -1, 7, 7, 7, 7, 0, 0, 0, -1, -1, 0, 0, -1, -1, -1,
    -1, -1, -1, -1, -1, 3, -1, -1, 3, -1, -1, -1, -1, 0, 0, 3, 3, 22, 22, 22, 7, 7, 7, 7, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 21, -1, -1, -1, -1, -1, -1, 22, 22, 22, 21,
    21, 5, 5, 5, -1, -1, 21, 21, -1, -1, 0, 0, 0, -1, 21, -1, -1, -1, 21, 8, 8, 8, 5, -1, -1,
    -1, -1, -1, -1, 22, 22, -1, -1, -1, -1, -1, -1, -1, 4, 4, -1, -1, -1, -1, -1, -1, -1, -1,
    21, 24, 7, 25, -1, -1, -1, -1, -1, 21, 21, -1, -1, -1, -1, -1, -1, 5, 5, 5, 5, 5, -1, 25,
    23, 20, -1, -1, 26, -1, -1
)
