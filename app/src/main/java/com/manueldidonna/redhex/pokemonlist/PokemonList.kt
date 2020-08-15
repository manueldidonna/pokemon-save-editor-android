package com.manueldidonna.redhex.pokemonlist

import androidx.compose.foundation.Box
import androidx.compose.foundation.ContentGravity
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ArrowCircleDown
import androidx.compose.material.icons.twotone.ArrowCircleUp
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.Storage
import com.manueldidonna.pk.core.StorageCollection
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSprite
import com.manueldidonna.redhex.common.SpritesRetrieverAmbient
import com.manueldidonna.redhex.common.rememberMutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PokemonList(
    modifier: Modifier = Modifier,
    collection: StorageCollection,
    showPokemonDetails: (Pokemon.Position) -> Unit,
) {
    // TODO: persist across navigation events
    var currentIndex by savedInstanceState { collection.indices.first }

    val pokemonPreviews by getPokemonPreviews(collection, currentIndex)

    Column(modifier = modifier) {
        AppBarWithStorageInfo(
            storageName = getStorage(collection, currentIndex).name,
            onBack = { currentIndex = currentIndex.decreaseInIndices(collection.indices) },
            onForward = { currentIndex = currentIndex.increaseInIndices(collection.indices) }
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
private fun getStorage(collection: StorageCollection, storageIndex: Int): Storage {
    return remember(storageIndex) { collection.getStorage(storageIndex) }
}

@Stable
@Composable
private fun getPokemonPreviews(
    collection: StorageCollection,
    storageIndex: Int,
): State<List<PokemonPreview>> {
    val previews = rememberMutableState(referentialEqualityPolicy()) { listOf<PokemonPreview>() }

    val resources = PokemonResourcesAmbient.current.natures
    val spritesRetriever = SpritesRetrieverAmbient.current

    launchInComposition(key = storageIndex) {
        withContext(Dispatchers.IO) {
            val storage = collection.getStorage(storageIndex)
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
                Icon(Icons.TwoTone.ArrowCircleUp)
            }
            IconButton(onClick = onForward) {
                Icon(Icons.TwoTone.ArrowCircleDown)
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
