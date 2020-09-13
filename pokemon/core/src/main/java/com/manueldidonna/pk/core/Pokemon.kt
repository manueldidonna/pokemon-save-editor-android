package com.manueldidonna.pk.core

import com.manueldidonna.pk.core.MutablePokemon.Mutator
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
         * @see StorageSystem.get
         */
        val storageIndex: Int,

        /**
         * The index of the [Pokemon] in the [Storage].
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
     * Get info about a specific move.
     * Use 'selectMove(index, Pokemon.Move::Immutable)' to get a [Move] instance
     *
     * Should throw an [IllegalStateException] if [index] is out of bounds [0 - 3]
     */
    fun <T> selectMove(index: Int, mapTo: (id: Int, powerPoints: Int, ups: Int) -> T): T

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
    }

    val form: Form?

    sealed class Form {
        /**
         * Accepted values for [letter] are A-Z and !, ? (since Generation III),
         * [letter] should be treated as case-insensitive.
         */
        data class Unown(val letter: Char) : Form()
    }

    val metInfo: MetInfo?

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
         * Return a new [Pokemon] instance from a [Template].
         * Should return null if [template] create an empty pokemon.
         *
         * @see isEmpty
         * @see Pokemon.position
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
 * @see Mutator
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

        fun individualValues(
            health: Int = -1,
            attack: Int = -1,
            defense: Int = -1,
            speed: Int = -1,
            specialAttack: Int = -1,
            specialDefense: Int = -1,
        ): Mutator

        fun effortValues(
            health: Int = -1,
            attack: Int = -1,
            defense: Int = -1,
            speed: Int = -1,
            specialAttack: Int = -1,
            specialDefense: Int = -1,
        ): Mutator

        fun form(value: Pokemon.Form): Mutator

        fun metInfo(value: MetInfo): Mutator
    }
}

fun Mutator.effortValues(all: Int): Mutator = apply {
    effortValues(all, all, all, all, all, all)
}

fun Mutator.individualValues(all: Int): Mutator = apply {
    individualValues(all, all, all, all, all, all)
}
