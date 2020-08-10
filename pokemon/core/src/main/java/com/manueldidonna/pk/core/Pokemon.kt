package com.manueldidonna.pk.core

/**
 * [Pokemon] represents a read-only entity. Its data will never change.
 *
 * If you need to mutate the Pokemon, use [Pokemon.asMutablePokemon]
 * @see [MutablePokemon]
 *
 */
interface Pokemon {
    val version: Version

    val position: Position

    data class Position(
        /**
         * @see [Storage.index]
         */
        val index: Int,

        /**
         * The position in the [Storage], in according to [Storage.capacity]
         */
        val slot: Int
    )

    val trainer: Trainer

    val isShiny: Boolean

    val speciesId: Int

    val nickname: String

    val level: Int

    val experiencePoints: Int

    val natureId: Int

    val heldItemId: Property<Int>

    val pokerus: Property<Pokerus>

    val friendship: Property<Int>

    /**
     * Get info about a specific move.
     * Use 'selectMove(index, Pokemon::Move)' to get a [Move] instance
     *
     * Should throw an [IllegalStateException] if [index] is out of bounds [0 - 3]
     */
    fun <T> selectMove(index: Int, mapTo: (id: Int, powerPoints: Int, ups: Int) -> T): T

    data class Move(
        val id: Int,
        val powerPoints: Int,
        val ups: Int
    ) {
        companion object {
            val Empty: Move = Move(id = 0, powerPoints = 0, ups = 0)
        }
    }

    val iV: StatisticValues

    val eV: StatisticValues

    // TODO: rename to Statistics
    interface StatisticValues {
        val health: Int
        val attack: Int
        val defense: Int
        val specialAttack: Int
        val specialDefense: Int
        val speed: Int
    }

    /**
     * Return null if the pokemon doesn't have an alternative form
     */
    val form: Form?

    sealed class Form {
        /**
         * Accepted values for [letter] are A-Z and !, ? (since Generation III)
         * [letter] should be case-insensitive.
         */
        data class Unown(val letter: Char) : Form()
    }

    val metInfo: Property<MetInfo>

    // TODO: rename to exportToBytes to be consistent with the SaveData
    fun asBytes(): UByteArray

    // TODO: rename to toMutablePokemon and return a new instance
    fun asMutablePokemon(): MutablePokemon
}

inline val Pokemon.isEmpty: Boolean
    get() = speciesId == 0 || nickname.isEmpty()

/**
 * [MutablePokemon] allows to mutate the pokemon data via a [MutablePokemon.Mutator],
 * it makes explicit the desire to modify the pokemon's data.
 *
 * A [MutablePokemon] can be under casted to [Pokemon] but it won't became immutable,
 * it will continue to reflects any data changes.
 * TODO: add toPokemon()
 */
interface MutablePokemon : Pokemon {

    /**
     * An interface to apply a predefined set of values to a [MutablePokemon]
     *
     * TODO: with kotlin 1.4 -> fun interface Template
     */
    interface Template {
        fun apply(pokemon: MutablePokemon)
    }

    /**
     * A [Mutator] makes explicit the desire to modify the pokemon's data
     */
    val mutator: Mutator

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
            specialDefense: Int = -1
        ): Mutator

        fun effortValues(
            health: Int = -1,
            attack: Int = -1,
            defense: Int = -1,
            speed: Int = -1,
            specialAttack: Int = -1,
            specialDefense: Int = -1
        ): Mutator

        fun form(value: Pokemon.Form): Mutator

        fun metInfo(value: MetInfo): Mutator
    }
}

fun MutablePokemon.Mutator.effortValues(all: Int): MutablePokemon.Mutator = apply {
    effortValues(all, all, all, all, all, all)
}

fun MutablePokemon.Mutator.individualValues(all: Int): MutablePokemon.Mutator = apply {
    individualValues(all, all, all, all, all, all)
}
