package com.manueldidonna.redhex

import androidx.compose.runtime.staticAmbientOf
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.SpritesRetriever

val AmbientPokemonTextResources = staticAmbientOf<PokemonTextResources>()

val AmbientSpritesRetriever = staticAmbientOf<SpritesRetriever>()
