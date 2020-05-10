package com.manueldidonna.pk.resources.english

import com.manueldidonna.pk.resources.PokemonResources

internal class EnglishPokemonResources : PokemonResources {
    override val natures: PokemonResources.Natures = EnglishNatures()

    override val moves: PokemonResources.Moves = EnglishMoves()
}