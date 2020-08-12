package com.manueldidonna.redhex.pokemonlist

import androidx.compose.foundation.Box
import androidx.compose.foundation.ContentGravity
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ChevronLeft
import androidx.compose.material.icons.twotone.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.StorageCollection
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSprite
import com.manueldidonna.redhex.common.SpritesRetrieverAmbient
import com.manueldidonna.redhex.common.ui.ToolbarHeight
import com.manueldidonna.redhex.common.ui.TranslucentToolbar

@Composable
fun PokemonList(
    modifier: Modifier = Modifier,
    collection: StorageCollection,
    showPokemonDetails: (Pokemon.Position) -> Unit,
) {
    val resources = PokemonResourcesAmbient.current.natures
    val spritesRetriever = SpritesRetrieverAmbient.current

    // TODO: persist across navigation events
    var currentIndex by savedInstanceState { collection.indices.first }

    val storage = remember(currentIndex) {
        collection.getMutableStorage(currentIndex)
    }

    val pokemonPreviews = remember(storage.index) {
        PokemonPreview.fromStorage(storage, resources, spritesRetriever).toMutableStateList()
    }

    Column(modifier = modifier) {
        Toolbar(
            modifier = Modifier.height(ToolbarHeight),
            title = storage.name,
            onBack = {
                currentIndex = decreaseIndex(currentIndex, collection.indices)
            },
            onForward = {
                currentIndex = increaseIndex(currentIndex, collection.indices)
            }
        )
        LazyColumnForIndexed(items = pokemonPreviews) { index, preview ->
            PokemonPreview(preview) {
                showPokemonDetails(Pokemon.Position(currentIndex, index))
            }
            Divider()
        }
    }
}

private fun increaseIndex(currentIndex: Int, indices: IntRange): Int {
    val index = currentIndex + 1
    return if (index > indices.last) indices.first else index
}

private fun decreaseIndex(currentIndex: Int, indices: IntRange): Int {
    val index = currentIndex - 1
    return if (index < indices.first) indices.last else index
}

@Composable
private fun Toolbar(
    modifier: Modifier = Modifier,
    title: String,
    onBack: () -> Unit,
    onForward: () -> Unit,
) {
    TranslucentToolbar(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        IconButton(onClick = onBack) {
            Icon(Icons.TwoTone.ChevronLeft, tint = MaterialTheme.colors.onSurface)
        }
        Text(
            modifier = Modifier
                .preferredWidthIn(minWidth = 150.dp)
                .wrapContentWidth(Alignment.CenterHorizontally),
            text = title
        )
        IconButton(onClick = onForward) {
            Icon(Icons.TwoTone.ChevronRight, tint = MaterialTheme.colors.onSurface)
        }
    }
}

@Composable
private fun PokemonPreview(preview: PokemonPreview, onSelection: () -> Unit) {
    ListItem(
        text = { Text(text = preview.nickname, color = pokemonPreviewTextColor(preview.isEmpty)) },
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
private fun pokemonPreviewTextColor(isEmptySlot: Boolean): Color {
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
