package com.manueldidonna.redhex.pokemonlist

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Dns
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
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
import com.manueldidonna.redhex.common.ui.ThemedDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PokemonList(
    modifier: Modifier = Modifier,
    // TODO: this is a workaround until I can't save this value across navigation events
    initialStorageIndex: Int,
    storageSystem: StorageSystem,
    editPokemonByPosition: (Pokemon.Position) -> Unit,
) {
    var currentIndex by savedInstanceState { initialStorageIndex }

    val pokemonPreviews by getPokemonPreviews(storageSystem, currentIndex)

    var changeIndex by rememberMutableState { false }

    Stack(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.matchParentSize()) {
            val storage = getStorage(storageSystem, currentIndex)
            TopAppBar(storageName = storage.name)
            LazyColumnForIndexed(items = pokemonPreviews) { slot, preview ->
                PokemonPreview(preview) {
                    editPokemonByPosition(Pokemon.Position(currentIndex, slot))
                }
                if (slot == storage.capacity - 1) {
                    // TODO: BUG with lazy row contentPadding
                    // fab: height + padding
                    Spacer(Modifier.height(72.dp))
                } else {
                    Divider()
                }
            }
        }
        ExtendedFloatingActionButton(
            modifier = Modifier.gravity(Alignment.BottomEnd).padding(16.dp),
            text = { Text("Change Storage") },
            icon = { Icon(asset = Icons.TwoTone.Dns) },
            onClick = { changeIndex = true }
        )
    }
    if (changeIndex) {
        ThemedDialog(onDismissRequest = { changeIndex = false }) {
            StorageSelector(storageSystem = storageSystem) { index ->
                currentIndex = index
                changeIndex = false
            }
        }
    }
}

@Composable
private fun TopAppBar(storageName: String) {
    TopAppBar(title = { Text(text = storageName) })
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
        previews.value = withContext(Dispatchers.IO) {
            val storage = storageSystem[storageIndex]
            PokemonPreview.fromStorage(storage, resources, spritesRetriever)
        }
    }

    return previews
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
        modifier = Modifier.clickable(onClick = onSelection),
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
