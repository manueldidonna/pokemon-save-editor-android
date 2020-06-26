package com.manueldidonna.redhex.common

import androidx.activity.result.ActivityResultRegistry
import androidx.compose.staticAmbientOf
import com.manueldidonna.pk.resources.text.PokemonTextResources

val ActivityResultRegistryAmbient = staticAmbientOf<ActivityResultRegistry>()

val PokemonResourcesAmbient = staticAmbientOf<PokemonTextResources>()

val SpritesRetrieverAmbient = staticAmbientOf<SpritesRetriever>()