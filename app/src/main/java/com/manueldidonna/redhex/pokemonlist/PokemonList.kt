package com.manueldidonna.redhex.pokemonlist

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.Divider
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CompareArrows
import androidx.compose.material.icons.twotone.LocalLibrary
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.Storage
import com.manueldidonna.pk.core.StorageSystem
import com.manueldidonna.redhex.common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface PokemonListEvents {
    fun editPokemonByPosition(position: Pokemon.Position)
    fun showPokedex()
}

@Composable
fun PokemonList(storageSystem: StorageSystem, events: PokemonListEvents) {
    // TODO: I should save this value across navigation events
    var storageIndex by savedInstanceState { storageSystem.storageIndices.first }

    val entries by getPokemonEntries(storageSystem, storageIndex)

    var changeIndex by rememberMutableState { false }

    Stack(modifier = Modifier.fillMaxSize()) {
        val storage = getStorage(storageSystem, storageIndex)
        TranslucentAppBar(
            title = storage.name,
            modifier = Modifier.zIndex(8f),
            actions = {
                IconButton(onClick = events::showPokedex) {
                    Icon(Icons.TwoTone.LocalLibrary)
                }
            }
        )
        LazyColumnForIndexed(items = entries) { pokemonIndex, entry ->
            if (pokemonIndex == 0) {
                // TODO: BUG with lazy column contentPadding
                Spacer(Modifier.height(AppBarHeight))
            }
            PokemonEntry(entry) {
                events.editPokemonByPosition(Pokemon.Position(storageIndex, pokemonIndex))
            }
            if (pokemonIndex == storage.capacity - 1) {
                // TODO: BUG with lazy column contentPadding
                // fab: height + padding
                Spacer(Modifier.height(72.dp))
            } else {
                Divider()
            }
        }
        ExtendedFloatingActionButton(
            modifier = Modifier.gravity(Alignment.BottomEnd).padding(16.dp),
            text = { Text("CHANGE") },
            icon = { Icon(asset = Icons.TwoTone.CompareArrows) },
            onClick = { changeIndex = true }
        )
    }
    if (changeIndex) {
        StorageSelectorDialog(
            storageSystem = storageSystem,
            onSelect = { index -> storageIndex = index },
            onDismissRequest = { changeIndex = false }
        )
    }
}

@Stable
@Composable
private fun getStorage(storageSystem: StorageSystem, storageIndex: Int): Storage {
    return remember(storageIndex) { storageSystem[storageIndex] }
}

@Stable
@Composable
private fun getPokemonEntries(
    storageSystem: StorageSystem,
    storageIndex: Int,
): State<List<PokemonEntry>> {
    val entries = rememberMutableState(referentialEqualityPolicy()) { listOf<PokemonEntry>() }
    val resources = PokemonResourcesAmbient.current.natures
    val spritesRetriever = SpritesRetrieverAmbient.current
    launchInComposition(key = storageIndex) {
        entries.value = withContext(Dispatchers.IO) {
            val storage = storageSystem[storageIndex]
            PokemonEntry.fromStorage(storage, resources, spritesRetriever)
        }
    }
    return entries
}

@Composable
private fun PokemonEntry(entry: PokemonEntry, onSelection: () -> Unit) {
    ListItemWithSprite(
        primaryText = entry.nickname,
        secondaryText = entry.label,
        spriteSource = entry.source,
        enabled = !entry.isEmpty,
        modifier = Modifier.clickable(onClick = onSelection)
    )
}
