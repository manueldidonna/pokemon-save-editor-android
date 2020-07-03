package com.manueldidonna.pk.core

/**
 * Pokerus is a microscopic life-form that may attach to [Pokemon].
 * Pokerus has been introduced in Gen 2 games.
 *
 * When a Pokemon has the Pokerus, it gains double the effort values from battling.
 * A Pokemon is or has been infected if the strain is a nonzero value.
 */
data class Pokerus(val strain: Int, val days: Int) {
    val isCured: Boolean = strain != 0 && days == 0

    companion object {
        /**
         * Allowed strain values for a Pokerus instance.
         *
         * 0 is omitted because it represents a Pokemon that has never been infected
         * @see NeverInfected
         */
        val StrainValues: IntRange = 1..15 // 0 means never infected

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
