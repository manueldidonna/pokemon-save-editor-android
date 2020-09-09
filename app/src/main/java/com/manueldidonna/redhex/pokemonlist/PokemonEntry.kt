package com.manueldidonna.redhex.pokemonlist

import androidx.compose.runtime.Immutable
import com.manueldidonna.pk.core.Storage
import com.manueldidonna.pk.core.isEmpty
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.SpriteSource
import com.manueldidonna.redhex.common.SpritesRetriever

@Immutable
data class PokemonEntry(
    val isEmpty: Boolean,
    val nickname: String,
    val label: String,
    val source: SpriteSource,
) {
    companion object {
        private val Empty = PokemonEntry(true, "Empty Slot", "", SpriteSource.PokeBall)

        fun fromStorage(
            storage: Storage,
            resources: PokemonTextResources.Natures,
            spritesRetriever: SpritesRetriever,
        ): List<PokemonEntry> {
            return List(storage.capacity) { i ->
                val pokemon = storage[i]
                if (pokemon.isEmpty) return@List Empty
                return@List PokemonEntry(
                    isEmpty = false,
                    nickname = pokemon.nickname,
                    label = "${resources.getNatureById(pokemon.natureId)} - Lv.${pokemon.level}",
                    source = spritesRetriever.getPokemonSprite(pokemon.speciesId, pokemon.isShiny)
                )
            }
        }
    }
}