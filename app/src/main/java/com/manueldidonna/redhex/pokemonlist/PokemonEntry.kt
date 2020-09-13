package com.manueldidonna.redhex.pokemonlist

import androidx.compose.runtime.Immutable
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.Storage
import com.manueldidonna.pk.core.isEmpty
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.SpriteSource
import com.manueldidonna.redhex.common.SpritesRetriever

@Immutable
data class PokemonEntry(
    val nickname: String,
    val label: String,
    val source: SpriteSource,
) {
    companion object {
        fun fromStorage(
            storage: Storage,
            resources: PokemonTextResources.Natures,
            spritesRetriever: SpritesRetriever,
        ): List<PokemonEntry?> {
            return List(storage.capacity) { i ->
                val pokemon = storage[i]
                return@List fromPokemon(pokemon, resources, spritesRetriever)
            }
        }

        fun fromPokemon(
            pokemon: Pokemon?,
            resources: PokemonTextResources.Natures,
            spritesRetriever: SpritesRetriever,
        ): PokemonEntry? {
            if (pokemon.isEmpty()) return null
            return PokemonEntry(
                nickname = pokemon.nickname,
                label = "${resources.getNatureById(pokemon.natureId)} - Lv.${pokemon.level}",
                source = spritesRetriever.getPokemonSprite(pokemon.speciesId, pokemon.isShiny)
            )
        }
    }
}