package com.manueldidonna.redhex

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.AssetsSpritesRetriever
import com.manueldidonna.redhex.common.SpritesRetriever

@Composable
fun ProvidePokemonResources(
    spritesRetriever: SpritesRetriever = AssetsSpritesRetriever,
    textResources: PokemonTextResources = PokemonTextResources.English,
    content: @Composable () -> Unit,
) {
    Providers(
        AmbientSpritesRetriever provides spritesRetriever,
        AmbientPokemonTextResources provides textResources,
        content = content
    )
}
