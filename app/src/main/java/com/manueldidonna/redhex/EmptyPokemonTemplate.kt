package com.manueldidonna.redhex

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.resources.text.PokemonTextResources

class EmptyPokemonTemplate(
    private val trainer: Trainer,
    private val resources: PokemonTextResources.Species,
) : MutablePokemon.Template {
    override fun apply(pokemon: MutablePokemon) {
        pokemon.mutator
            .speciesId(getSpeciesId(pokemon.version))
            .level(1)
            .experiencePoints(0)
            .move(index = 0, move = Pokemon.Move.Immutable(id = 1, powerPoints = 999, ups = 3))
            .move(index = 1, move = Pokemon.Move.Empty)
            .move(index = 2, move = Pokemon.Move.Empty)
            .move(index = 3, move = Pokemon.Move.Empty)
            .nickname(resources.getSpeciesById(pokemon.speciesId), ignoreCase = true)
            .trainer(trainer)
            .individualValues(all = 15)
            .effortValues(all = 999999)
            .shiny(false)
            .friendship(0)
            .pokerus(Pokerus.NeverInfected)
            .heldItemId(0)
    }

    private fun getSpeciesId(version: Version): Int {
        return when (version.generation) {
            1 -> 151
            2 -> 251
            else -> throw IllegalStateException("Unsupported version $version")
        }
    }
}