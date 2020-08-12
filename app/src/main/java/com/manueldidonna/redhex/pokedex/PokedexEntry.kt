package com.manueldidonna.redhex.pokedex

import androidx.compose.runtime.Immutable
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.SpriteSource
import com.manueldidonna.redhex.common.SpritesRetriever

@Immutable
class PokedexEntry private constructor(
    override val speciesId: Int,
    override val isSeen: Boolean,
    override val isOwned: Boolean,
) : Pokedex.Entry {
    lateinit var name: String
        private set
    var source: SpriteSource = SpriteSource.PokeBall
        private set

    fun copy(isSeen: Boolean, isOwned: Boolean): PokedexEntry {
        return PokedexEntry(speciesId, isSeen, isOwned).apply {
            this.name = this@PokedexEntry.name
            this.source = this@PokedexEntry.source
        }
    }

    companion object {
        fun getAllFromPokedex(
            pokedex: Pokedex,
            spritesRetriever: SpritesRetriever,
            resources: PokemonTextResources.Species
        ): List<PokedexEntry> {
            return List(pokedex.pokemonCount) {
                pokedex
                    .selectEntry(speciesId = it + 1, mapTo = ::PokedexEntry)
                    .apply {
                        name = resources.getSpeciesById(speciesId)
                        source = spritesRetriever.getPokemonSprite(speciesId, shiny = false)
                    }
            }
        }
    }
}