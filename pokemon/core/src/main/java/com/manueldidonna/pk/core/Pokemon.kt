package com.manueldidonna.pk.core

/**
 * [Pokemon] represents a read-only entity. Its data will never change.
 *
 * An immutable [Pokemon] cannot be casted to [MutablePokemon].
 * Each [Pokemon] implementation must enforces this rule.
 */
interface Pokemon {
    val isEmpty: Boolean

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

    val speciesId: Int

    val nickname: String

    val level: Int

    val experiencePoints: Int

    val natureId: Int

    val moves: Moves

    interface Moves {
        fun getId(index: Int): Int
        fun getPowerPoints(index: Int): Int
        fun getUps(index: Int): Int
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

    val status: StatusCondition?

    enum class StatusCondition {
        Asleep, Poisoned, Burned, Frozen, Paralyzed
    }
}

/**
 * [MutablePokemon] allows to mutate the pokemon data via a [MutablePokemon.Mutator],
 * it makes explicit the desire to modify the pokemon's data.
 *
 * [MutablePokemon] doesn't represent a specific pokemon, it will always reflects
 * the current data values at [position].
 * Ex: if you delete the pokemon at [position], also [MutablePokemon] instance data will be erased.
 *
 * A [MutablePokemon] can be under casted to [Pokemon] but it won't became immutable,
 * it will continue to reflects any data changes at [position].
 *
 * To get a real immutable instance use [Box.getPokemon]
 *
 * TODO: add a function like "asPokemon()" to convert MutablePokemon to Pokemon
 */
interface MutablePokemon : Pokemon {

    /**
     * An interface to apply a predefined set of values to a [MutablePokemon]
     */
    interface Template {
        val name: String
        val description: String

        // TODO: remove speciesId or add a property like 'Type' or 'Info'
        val speciesId: Int
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

        fun moveId(id: Int, moveIndex: Int): Mutator

        fun movePowerPoints(moveIndex: Int, moveId: Int = -1, points: Int = -1): Mutator

        // TODO: merge this and movePowerPoints() in the same function
        fun movePowerPointUps(moveIndex: Int, moveId: Int, ups: Int): Mutator

        fun status(value: Pokemon.StatusCondition?): Mutator

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
    }
}

fun MutablePokemon.Mutator.effortValues(all: Int): MutablePokemon.Mutator = apply {
    effortValues(all, all, all, all, all, all)
}

fun MutablePokemon.Mutator.individualValues(all: Int): MutablePokemon.Mutator = apply {
    individualValues(all, all, all, all, all, all)
}
