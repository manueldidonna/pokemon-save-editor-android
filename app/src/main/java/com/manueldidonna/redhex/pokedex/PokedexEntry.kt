package com.manueldidonna.redhex.pokedex

import androidx.compose.runtime.Immutable
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.SpriteSource
import com.manueldidonna.redhex.common.SpritesRetriever

@Immutable
data class PokedexEntry(
    override val speciesId: Int,
    override val isSeen: Boolean,
    override val isOwned: Boolean,
    val name: String,
    val source: SpriteSource,
) : Pokedex.Entry {

    fun copy(isSeen: Boolean, isOwned: Boolean): PokedexEntry {
        return PokedexEntry(speciesId, isSeen, isOwned, name, source)
    }

    companion object {
        fun getAllFromPokedex(
            pokedex: Pokedex,
            spritesRetriever: SpritesRetriever,
            resources: PokemonTextResources.Species,
        ): List<PokedexEntry> {
            val createEntry: (Int, Boolean, Boolean) -> PokedexEntry =
                { speciesId, isSeen, isOwned ->
                    val source = spritesRetriever.getPokemonSprite(speciesId, shiny = false)
                    val name = resources.getSpeciesById(speciesId)
                    PokedexEntry(speciesId, isSeen, isOwned, name, source)
                }
            return List(pokedex.pokemonCount) {
                pokedex.selectEntry(speciesId = it + 1, mapTo = createEntry)
            }
        }
    }
}
