package com.manueldidonna.redhex

import androidx.activity.result.ActivityResultRegistry
import androidx.compose.staticAmbientOf
import com.manueldidonna.pk.resources.PokemonResources

val ActivityResultRegistryAmbient = staticAmbientOf<ActivityResultRegistry>()

val PokemonResourcesAmbient = staticAmbientOf<PokemonResources>()