package com.manueldidonna.redhex.common

import androidx.activity.result.ActivityResultRegistry
import androidx.compose.staticAmbientOf
import com.manueldidonna.pk.resources.PokemonResources
import com.manueldidonna.redhex.common.pokemon.PokemonSpritesRetriever

val ActivityResultRegistryAmbient = staticAmbientOf<ActivityResultRegistry>()

val PokemonResourcesAmbient = staticAmbientOf<PokemonResources>()

val PokemonSpritesRetrieverAmbient = staticAmbientOf<PokemonSpritesRetriever>()