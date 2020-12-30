package com.manueldidonna.pk.core

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * A read-only entity that holds some properties.
 * Nullable properties may not exist in some games.
 */
interface Pokemon {
    val version: Version

    val position: Position

    data class Position(
        /**
         * The index of the [Storage] in the [StorageSystem].
         *
         * @see StorageSystem.get
         */
        val storageIndex: Int,

        /**
         * The index of the [Pokemon] in the [Storage].
         *
         * @see Storage.get
         */
        val pokemonIndex: Int,
    )

    val trainer: Trainer

    val isShiny: Boolean

    val speciesId: Int

    val nickname: String

    val level: Int

    val experiencePoints: Int

    val natureId: Int

    val heldItemId: Int?

    val pokerus: Pokerus?

    val friendship: Int?

    /**
     * Get [Move] properties at [index] and map them to [M] with [mapper].
     * Index cannot be grater than 3 or negative.
     */
    fun <M> selectMove(index: Int, mapper: MoveMapper<M>): M

    /**
     * Map [Move] properties to a generic type [M]
     */
    fun interface MoveMapper<M> {
        fun mapTo(id: Int, powerPoints: Int, ups: Int): M
    }

    interface Move {
        val id: Int
        val powerPoints: Int
        val ups: Int

        data class Immutable(
            override val id: Int,
            override val powerPoints: Int,
            override val ups: Int,
        ) : Move

        companion object {
            val Empty: Move = Immutable(id = 0, powerPoints = 0, ups = 0)
        }
    }

    val iV: StatisticValues

    val eV: StatisticValues

    interface StatisticValues {
        val health: Int
        val attack: Int
        val defense: Int
        val specialAttack: Int
        val specialDefense: Int
        val speed: Int

        data class Immutable(
            override val health: Int = -1,
            override val attack: Int = -1,
            override val defense: Int = -1,
            override val specialAttack: Int = -1,
            override val specialDefense: Int = -1,
            override val speed: Int = -1,
        ) : StatisticValues
    }

    val form: Form?

    sealed class Form {
        /**
         * Accepted values for [letter] are A-Z and !, ? (since Generation III),
         * [letter] should be treated as case-insensitive.
         */
        data class Unown(val letter: Char) : Form()
    }

    val caughtData: CaughtData?

    data class CaughtData(
        val level: Int,
        val time: Time,
        val locationId: Int,
    ) {
        sealed class Time {
            sealed class TimeOfDay : Time() {
                object Morning : TimeOfDay()
                object DayTime : TimeOfDay()
                object Night : TimeOfDay()
            }
        }
    }

    /**
     * Return a new [MutablePokemon] instance.
     */
    fun toMutablePokemon(): MutablePokemon

    /**
     * Return a bytes representation of this pokemon.
     */
    fun exportToBytes(): UByteArray

    /**
     * An interface to apply a predefined set of values to a [Pokemon]
     */
    fun interface Template {
        /**
         * Return a new [Pokemon] instance.
         */
        fun applyTo(pokemon: Pokemon): Pokemon
    }

    /**
     * An interface to create a valid [Pokemon] instance.
     */
    interface Factory {
        /**
         * Return a new [Pokemon] instance applying a [Template].
         * Should return null if [template] create an empty pokemon.
         *
         * @see Pokemon.Template
         * @see isEmpty
         */
        fun create(template: Template, position: Position): Pokemon?
    }
}

@OptIn(ExperimentalContracts::class)
fun Pokemon?.isEmpty(): Boolean {
    contract {
        returns(false) implies (this@isEmpty != null)
    }
    return this == null || speciesId == 0
}

/**
 * A mutable variant of [Pokemon].
 */
interface MutablePokemon : Pokemon {
    /**
     * A [Mutator] makes explicit the desire to modify the pokemon's data
     */
    val mutator: Mutator

    /**
     * Allows to modify the pokemon data with a fluent interface.
     */
    interface Mutator {
        fun speciesId(value: Int): Mutator

        fun nickname(value: String, ignoreCase: Boolean = false): Mutator

        fun trainer(value: Trainer, ignoreNameCase: Boolean = false): Mutator

        fun experiencePoints(value: Int): Mutator

        fun level(value: Int): Mutator

        fun move(index: Int, move: Pokemon.Move): Mutator

        fun shiny(value: Boolean): Mutator

        fun heldItemId(value: Int): Mutator

        fun pokerus(value: Pokerus): Mutator

        fun friendship(value: Int): Mutator

        fun individualValues(value: Pokemon.StatisticValues): Mutator

        fun effortValues(value: Pokemon.StatisticValues): Mutator

        fun form(value: Pokemon.Form): Mutator

        fun caughtData(value: Pokemon.CaughtData): Mutator
    }
}
