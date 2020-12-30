package com.manueldidonna.pk.core

/**
 * Pokerus ia a pokemon property introduced in Generation II games.
 *
 * A Pokemon is or has been infected if [strain] is a nonzero positive value.
 *
 * @see StrainValues
 * @see maxAllowedDays
 * @see NeverInfected
 */
data class Pokerus(val strain: Int, val days: Int) {
    companion object {
        /**
         * Allowed strain values for a valid Pokerus instance.
         */
        val StrainValues: IntRange = 1..15

        /**
         * The max number of days before a Pokemon will be cured
         */
        fun maxAllowedDays(strain: Int): Int = strain % 4 + 1

        /**
         * A singleton instance for a Pokemon that has never been infected by the Pokerus
         */
        val NeverInfected = Pokerus(strain = 0, days = 0)
    }
}
