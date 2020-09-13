package com.manueldidonna.redhex.pokemonlist

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CompareArrows
import androidx.compose.material.icons.twotone.LocalLibrary
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.HapticFeedBackAmbient
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.StorageSystem
import com.manueldidonna.redhex.common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface PokemonListEvents {
    fun editPokemonByPosition(position: Pokemon.Position)
    fun showPokedex()
}

@Composable
fun PokemonList(storageSystem: StorageSystem, events: PokemonListEvents) {
    // TODO: I should save this value across navigation events
    var storageIndex by savedInstanceState { storageSystem.storageIndices.first }

    val storage = getStorage(storageSystem, storageIndex)

    var changeIndex by rememberMutableState { false }

    onCommit(storageIndex) {
        storage.value.fetchAll()
    }

    val coroutineScope = rememberCoroutineScope()

    Stack(modifier = Modifier.fillMaxSize()) {
        TranslucentAppBar(
            title = storage.value.name,
            modifier = Modifier.zIndex(8f),
            actions = {
                IconButton(onClick = events::showPokedex) {
                    Icon(Icons.TwoTone.LocalLibrary)
                }
            }
        )
        LazyColumnForIndexed(items = storage.value.entries) { pokemonIndex, entry: PokemonEntry? ->
            if (pokemonIndex == 0) {
                // TODO: BUG with lazy column contentPadding
                Spacer(Modifier.height(AppBarHeight))
            }
            PokemonEntry(
                entry = entry,
                onSelection = {
                    events.editPokemonByPosition(Pokemon.Position(storageIndex, pokemonIndex))
                },
                onDeletion = {
                    coroutineScope.launch(Dispatchers.Default) {
                        storage.value.removeAt(pokemonIndex)
                        storageSystem[storageIndex] = storage.value
                    }
                }
            )
            if (pokemonIndex == storage.value.capacity - 1) {
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
private fun getStorage(
    storageSystem: StorageSystem,
    storageIndex: Int,
): State<ObservableStorage> {
    val resources = PokemonResourcesAmbient.current.natures
    val spritesRetriever = SpritesRetrieverAmbient.current
    return rememberMutableStateFor(storageIndex) {
        val storage = storageSystem[storageIndex].toMutableStorage()
        ObservableStorage(storage, resources, spritesRetriever)
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun PokemonEntry(entry: PokemonEntry?, onSelection: () -> Unit, onDeletion: () -> Unit) {
    if (entry == null) {
        EmptyPokemonEntry(onSelection)
        return
    }
    var expandDropDownMenu by rememberMutableState { false }
    val hapticFeedback = HapticFeedBackAmbient.current
    DropdownMenu(
        toggle = {
            ListItemWithSprite(
                primaryText = entry.nickname,
                secondaryText = entry.label,
                spriteSource = entry.source,
                modifier = Modifier
                    .clickable(
                        onClick = onSelection,
                        onLongClick = {
                            expandDropDownMenu = true
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    ).background(MaterialTheme.colors.surface)
            )
        },
        expanded = expandDropDownMenu,
        onDismissRequest = { expandDropDownMenu = false }
    ) {
        DropdownMenuItem(onClick = {
            onDeletion()
            expandDropDownMenu = false
        }) {
            Text("Delete")
        }
    }
}

@Composable
private fun EmptyPokemonEntry(onSelection: () -> Unit) {
    ListItemWithSprite(
        primaryText = "Empty Slot",
        spriteSource = SpriteSource.PokeBall,
        enabled = false,
        modifier = Modifier.clickable(onClick = onSelection)
    )
}
