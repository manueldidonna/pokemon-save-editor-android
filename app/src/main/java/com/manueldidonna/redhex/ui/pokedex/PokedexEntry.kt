package com.manueldidonna.redhex.ui.pokedex

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.redhex.AmbientPokemonTextResources
import com.manueldidonna.redhex.AmbientSpritesRetriever

@Immutable
data class PokedexEntry(
    override val speciesId: Int,
    override val isSeen: Boolean,
    override val isOwned: Boolean,
    val speciesName: String,
    val spriteSource: Any,
) : Pokedex.Entry


@Composable
fun pokedexEntryMapper(): Pokedex.EntryMapper<PokedexEntry> {
    val pokemonTextResources = AmbientPokemonTextResources.current.species
    val spritesRetriever = AmbientSpritesRetriever.current
    return remember(pokemonTextResources, spritesRetriever) {
        Pokedex.EntryMapper { speciesId, isSeen, isOwned ->
            PokedexEntry(
                speciesId = speciesId,
                isSeen = isSeen,
                isOwned = isOwned,
                speciesName = pokemonTextResources.getSpeciesById(speciesId),
                spriteSource = spritesRetriever.getPokemonSprite(speciesId, false)
            )
        }
    }
}
