package com.manueldidonna.redhex.pokemonlist

import androidx.compose.foundation.Box
import androidx.compose.foundation.ContentGravity
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ChevronLeft
import androidx.compose.material.icons.twotone.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.Storage
import com.manueldidonna.pk.core.StorageSystem
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSprite
import com.manueldidonna.redhex.common.SpritesRetrieverAmbient
import com.manueldidonna.redhex.common.rememberMutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PokemonList(
    modifier: Modifier = Modifier,
    // TODO: this is a workaround until I can't save this value across navigation events
    initialStorageIndex: Int,
    storageSystem: StorageSystem,
    showPokemonDetails: (Pokemon.Position) -> Unit,
) {
    var currentIndex by savedInstanceState { initialStorageIndex }

    val pokemonPreviews by getPokemonPreviews(storageSystem, currentIndex)

    Column(modifier = modifier) {
        AppBarWithStorageInfo(
            storageName = getStorage(storageSystem, currentIndex).name,
            onBack = {
                currentIndex = currentIndex.decreaseInIndices(storageSystem.storageIndices)
            },
            onForward = {
                currentIndex = currentIndex.increaseInIndices(storageSystem.storageIndices)
            }
        )
        LazyColumnForIndexed(items = pokemonPreviews) { slot, preview ->
            PokemonPreview(preview) {
                showPokemonDetails(Pokemon.Position(currentIndex, slot))
            }
            Divider()
        }
    }
}

@Stable
@Composable
private fun getStorage(storageSystem: StorageSystem, storageIndex: Int): Storage {
    return remember(storageIndex) { storageSystem[storageIndex] }
}

@Stable
@Composable
private fun getPokemonPreviews(
    storageSystem: StorageSystem,
    storageIndex: Int,
): State<List<PokemonPreview>> {
    val previews = rememberMutableState(referentialEqualityPolicy()) { listOf<PokemonPreview>() }

    val resources = PokemonResourcesAmbient.current.natures
    val spritesRetriever = SpritesRetrieverAmbient.current

    launchInComposition(key = storageIndex) {
        withContext(Dispatchers.IO) {
            val storage = storageSystem[storageIndex]
            previews.value = PokemonPreview.fromStorage(storage, resources, spritesRetriever)
        }
    }

    return previews
}

private fun Int.increaseInIndices(indices: IntRange): Int {
    val index = this + 1
    return if (index > indices.last) indices.first else index
}

private fun Int.decreaseInIndices(indices: IntRange): Int {
    val index = this - 1
    return if (index < indices.first) indices.last else index
}

@Composable
private fun AppBarWithStorageInfo(storageName: String, onBack: () -> Unit, onForward: () -> Unit) {
    TopAppBar(
        title = { Text(text = storageName) },
        actions = {
            IconButton(onClick = onBack) {
                Icon(Icons.TwoTone.ChevronLeft)
            }
            IconButton(onClick = onForward) {
                Icon(Icons.TwoTone.ChevronRight)
            }
        }
    )
}

@Composable
private fun PokemonPreview(preview: PokemonPreview, onSelection: () -> Unit) {
    ListItem(
        text = { Text(text = preview.nickname, color = pokemonNameColor(preview.isEmpty)) },
        icon = {
            Box(gravity = ContentGravity.Center, modifier = Modifier.size(40.dp)) {
                PokemonSprite(source = preview.source)
            }
        },
        onClick = onSelection,
        secondaryText = pokemonPreviewCharacteristics(preview.label)
    )

}

@Stable
@Composable
private fun pokemonNameColor(isEmptySlot: Boolean): Color {
    val surfaceColor = MaterialTheme.colors.onSurface
    if (!isEmptySlot) return surfaceColor
    return EmphasisAmbient.current.disabled.applyEmphasis(surfaceColor)
}

@Stable
@Composable
private fun pokemonPreviewCharacteristics(text: String): @Composable (() -> Unit)? {
    if (text.isEmpty()) return null
    return {
        Text(text = text)
    }
}
