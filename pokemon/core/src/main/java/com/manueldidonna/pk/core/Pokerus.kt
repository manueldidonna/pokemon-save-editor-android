package com.manueldidonna.pk.core

/**
 * Pokerus has been introduced in Generation II games.
 *
 * A Pokemon is or has been infected if the strain is a nonzero value.
 * [days] should be coerced in [0 - [maxAllowedDays]]
 */
data class Pokerus(val strain: Int, val days: Int) {
    companion object {
        /**
         * Allowed strain values for a Pokerus instance.
         *
         * 0 is omitted because it represents a Pokemon that has never been infected
         * @see NeverInfected
         */
        val StrainValues: IntRange = 1..15

        /**
         * The max number of days before a Pokemon will be cured depends on the strain
         */
        fun maxAllowedDays(strain: Int): Int = strain % 4 + 1

        /**
         * A singleton instance for a Pokemon that has never been infected by the Pokerus
         */
        val NeverInfected = Pokerus(strain = 0, days = 0)
    }
}
