package com.manueldidonna.pk.resources.text.english

import com.manueldidonna.pk.resources.text.PokemonTextResources

internal class EnglishPokemonTextResources : PokemonTextResources {
    override val natures: PokemonTextResources.Natures = EnglishNatures()
    override val moves: PokemonTextResources.Moves = EnglishMoves()
    override val species: PokemonTextResources.Species = EnglishSpecies()
    override val items: PokemonTextResources.Items = EnglishItems()
}