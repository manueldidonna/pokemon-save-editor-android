package com.manueldidonna.redhex.editor

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.Pokerus
import com.manueldidonna.pk.core.Trainer

@Stable
class ObservablePokemon(private val pokemon: MutablePokemon) : MutablePokemon by pokemon {

    override val speciesId get() = throw IllegalStateException()
    override val isShiny get() = throw IllegalStateException()
    override val nickname get() = throw IllegalStateException()
    override val form get() = throw IllegalStateException()

    override var pokerus: Pokerus? by mutableStateOf(pokemon.pokerus)
    override var friendship: Int? by mutableStateOf(pokemon.friendship)
    override val eV: StatisticValues = StatisticValues(pokemon.eV)
    override val iV: StatisticValues = StatisticValues(pokemon.iV)

    override var level by mutableStateOf(pokemon.level)
        private set

    override var experiencePoints by mutableStateOf(pokemon.experiencePoints)
        private set

    override var trainer by mutableStateOf(pokemon.trainer)

    var species: Species by mutableStateOf(Species(pokemon))
        private set

    var wrappedForm by mutableStateOf(FormWrapper(pokemon.form))
        private set

    val moves: SnapshotStateList<Move> =
        mutableStateListOf(
            pokemon.selectMove(0, ::Move),
            pokemon.selectMove(1, ::Move),
            pokemon.selectMove(2, ::Move),
            pokemon.selectMove(3, ::Move)
        )

    private val realMutator = pokemon.mutator

    override val mutator = object : MutablePokemon.Mutator by realMutator {
        override fun speciesId(value: Int): MutablePokemon.Mutator = apply {
            realMutator.speciesId(value)
            species = Species(pokemon)
        }

        override fun nickname(value: String, ignoreCase: Boolean): MutablePokemon.Mutator = apply {
            realMutator.nickname(value, ignoreCase)
            species = Species(pokemon)
        }

        override fun shiny(value: Boolean): MutablePokemon.Mutator = apply {
            realMutator.shiny(value)
            species = Species(pokemon)
            iV.update(pokemon.iV)
        }

        override fun level(value: Int): MutablePokemon.Mutator = apply {
            realMutator.level(value)
            level = pokemon.level
            experiencePoints = pokemon.experiencePoints
        }

        override fun experiencePoints(value: Int): MutablePokemon.Mutator = apply {
            realMutator.experiencePoints(value)
            level = pokemon.level
            experiencePoints = pokemon.experiencePoints
        }

        override fun pokerus(value: Pokerus): MutablePokemon.Mutator = apply {
            realMutator.pokerus(value)
            pokerus = pokemon.pokerus
        }

        override fun friendship(value: Int): MutablePokemon.Mutator = apply {
            realMutator.friendship(value)
            friendship = pokemon.friendship
        }

        override fun form(value: Pokemon.Form): MutablePokemon.Mutator = apply {
            realMutator.form(value)
            wrappedForm = FormWrapper(pokemon.form)
        }

        override fun trainer(
            value: Trainer,
            ignoreNameCase: Boolean,
        ): MutablePokemon.Mutator = apply {
            realMutator.trainer(value, ignoreNameCase)
            trainer = pokemon.trainer
        }

        override fun effortValues(
            health: Int,
            attack: Int,
            defense: Int,
            speed: Int,
            specialAttack: Int,
            specialDefense: Int,
        ): MutablePokemon.Mutator = apply {
            realMutator.effortValues(health, attack, defense, speed, specialAttack, specialDefense)
            eV.update(pokemon.eV)
        }

        override fun individualValues(
            health: Int,
            attack: Int,
            defense: Int,
            speed: Int,
            specialAttack: Int,
            specialDefense: Int,
        ): MutablePokemon.Mutator = apply {
            realMutator
                .individualValues(health, attack, defense, speed, specialAttack, specialDefense)
            iV.update(pokemon.iV)
        }

        override fun move(index: Int, move: Pokemon.Move): MutablePokemon.Mutator = apply {
            realMutator.move(index, move)
            moves[index] = pokemon.selectMove(index, ::Move)
        }
    }

    @Immutable
    data class Species(
        val id: Int,
        val nickname: String,
        val isShiny: Boolean,
    ) {
        constructor(pokemon: Pokemon) : this(
            id = pokemon.speciesId,
            nickname = pokemon.nickname,
            isShiny = pokemon.isShiny
        )
    }

    @Immutable
    data class FormWrapper(val form: Pokemon.Form?)

    @Immutable
    data class Move(
        override val id: Int,
        override val powerPoints: Int,
        override val ups: Int,
    ) : Pokemon.Move

    @Stable
    class StatisticValues(stats: Pokemon.StatisticValues) : Pokemon.StatisticValues {
        override var health by mutableStateOf(stats.health)
        override var attack by mutableStateOf(stats.attack)
        override var defense by mutableStateOf(stats.defense)
        override var specialAttack by mutableStateOf(stats.specialAttack)
        override var specialDefense by mutableStateOf(stats.specialDefense)
        override var speed by mutableStateOf(stats.speed)

        fun update(stats: Pokemon.StatisticValues) {
            health = stats.health
            attack = stats.attack
            defense = stats.defense
            specialAttack = stats.specialAttack
            specialDefense = stats.specialDefense
            speed = stats.speed
        }
    }
}
