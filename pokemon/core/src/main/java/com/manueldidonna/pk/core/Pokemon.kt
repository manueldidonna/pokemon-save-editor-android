package com.manueldidonna.pk.core

/**
 * [Pokemon] represents a read-only entity. Its data will never change.
 *
 * If you need to mutate the Pokemon, use [Pokemon.asMutablePokemon]
 * @see [MutablePokemon]
 *
 * TODO: add clone()
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
        get() = Property.Nothing

    val pokerus: Property<Pokerus>
        get() = Property.Nothing

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

            fun maxPowerPoints(id: Int, ups: Int = 3): Move {
                return Move(id = id, powerPoints = 999, ups = ups)
            }
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

    fun asBytes(): UByteArray

    // TODO: decide if this method should create a new instance or not
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

        fun move(index: Int, move: Pokemon.Move): Mutator

        fun shiny(value: Boolean): Mutator

        fun heldItemId(value: Int): Mutator = unsupportedProperty()

        fun pokerus(pokerus: Pokerus): Mutator = unsupportedProperty()

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

private fun unsupportedProperty(): Nothing {
    throw IllegalAccessException("Unsupported Pokemon Property")
}

fun MutablePokemon.Mutator.effortValues(all: Int): MutablePokemon.Mutator = apply {
    effortValues(all, all, all, all, all, all)
}

fun MutablePokemon.Mutator.individualValues(all: Int): MutablePokemon.Mutator = apply {
    individualValues(all, all, all, all, all, all)
}
