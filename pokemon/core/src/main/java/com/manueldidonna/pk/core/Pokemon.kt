package com.manueldidonna.pk.core

/**
 * [Pokemon] represents a read-only entity. Its data will never change.
 *
 * An immutable [Pokemon] cannot be casted to [MutablePokemon].
 * Each [Pokemon] implementation must enforces this rule.
 */
interface Pokemon {
    val position: Position

    data class Position(
        val index: StorageIndex,
        val slot: Int
    )

    val trainerId: UInt
    val trainerName: String

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

    val iV: IndividualValues

    interface IndividualValues {
        val health: Int
        val attack: Int
        val defense: Int
        val specialAttack: Int
        val specialDefense: Int
        val speed: Int
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
    val mutator: Mutator

    /**
     * A [Mutator] makes explicit the desire to modify the pokemon's data
     */
    interface Mutator {
        fun speciesId(value: Int): Mutator
        fun nickname(value: String): Mutator
        fun trainerId(value: UInt): Mutator
        fun trainerName(value: String): Mutator
        fun experiencePoints(value: Int): Mutator
        fun level(value: Int): Mutator
        fun moveId(id: Int, moveIndex: Int): Mutator
        /**
         * Points should be coerced to the maximum allowed value
         * Pass -1 as moveId to ignore the maximum allowed points of a specific move
         */
        fun movePowerPoints(moveIndex: Int, moveId: Int, points: Int): Mutator

        fun movePowerPointUps(moveIndex: Int, moveId: Int, ups: Int): Mutator
    }
}
