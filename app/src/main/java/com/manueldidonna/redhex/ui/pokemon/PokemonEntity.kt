package com.manueldidonna.redhex.ui.pokemon

import androidx.compose.runtime.*
import com.manueldidonna.pk.core.StorageSystem
import com.manueldidonna.pk.core.isEmpty
import com.manueldidonna.redhex.AmbientSpritesRetriever
import com.manueldidonna.redhex.common.rememberMutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Immutable
data class PokemonEntity(
    val nickname: String,
    val spriteSource: Any
)

@Composable
fun StorageSystem.getPokemonEntitiesAsState(storageIndex: Int): State<List<PokemonEntity?>> {
    val spritesRetriever = AmbientSpritesRetriever.current
    val entries = rememberMutableState { emptyList<PokemonEntity?>() }
    LaunchedEffect(subject = storageIndex) {
        val storage = this@getPokemonEntitiesAsState[storageIndex]
        entries.value = withContext(Dispatchers.Default) {
            List(storage.capacity) { index ->
                val pokemon = storage[index]
                if (pokemon.isEmpty()) return@List null
                PokemonEntity(
                    nickname = pokemon.nickname,
                    spriteSource = spritesRetriever.getPokemonSprite(
                        speciesId = pokemon.speciesId,
                        shiny = pokemon.isShiny
                    )
                )
            }
        }
    }
    return entries
}
