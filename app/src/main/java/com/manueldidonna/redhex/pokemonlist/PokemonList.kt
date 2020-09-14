package com.manueldidonna.redhex.pokemonlist

import androidx.compose.foundation.Box
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CompareArrows
import androidx.compose.material.icons.twotone.DeleteForever
import androidx.compose.material.icons.twotone.Edit
import androidx.compose.material.icons.twotone.LocalLibrary
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.StorageSystem
import com.manueldidonna.redhex.common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

interface StorageSystemEvents {
    fun editPokemonByPosition(position: Pokemon.Position)
    fun showPokedex()
}

@Composable
fun StorageSystem(storageSystem: StorageSystem, events: StorageSystemEvents) {
    val coroutineScope = rememberCoroutineScope()

    // TODO: I should save this value across navigation events
    var storageIndex by savedInstanceState { storageSystem.storageIndices.first }

    val storage = getStorage(storageSystem, storageIndex)

    // TODO: replace onCommit with launchInComposition in alpha03
    onCommit(storageIndex) {
        storage.value.fetchAll()
    }

    var changeStorageIndex by rememberMutableState { false }

    if (changeStorageIndex)
        StorageSelectorDialog(
            storageSystem = storageSystem,
            onSelect = { index -> storageIndex = index },
            onDismissRequest = { changeStorageIndex = false }
        )

    Stack(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier.zIndex(8f),
            storageName = storage.value.name,
            showPokedex = events::showPokedex
        )
        PokemonList(
            entries = storage.value.entries,
            editPokemonByIndex = { pokemonIndex ->
                events.editPokemonByPosition(Pokemon.Position(storageIndex, pokemonIndex))
            },
            deletePokemonByIndex = { pokemonIndex ->
                coroutineScope.launch(Dispatchers.Default) {
                    storage.value.removeAt(pokemonIndex)
                    storageSystem[storageIndex] = storage.value
                }
            }
        )
        ChangeStorageIndexButton(
            modifier = Modifier.gravity(Alignment.BottomEnd).padding(16.dp),
            onClick = { changeStorageIndex = true }
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
private fun TopAppBar(modifier: Modifier, storageName: String, showPokedex: () -> Unit) {
    TranslucentAppBar(
        title = storageName,
        modifier = modifier,
        actions = {
            IconButton(onClick = showPokedex) {
                Icon(Icons.TwoTone.LocalLibrary)
            }
        }
    )
}

@Composable
private fun ChangeStorageIndexButton(modifier: Modifier, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        text = { Text("CHANGE") },
        icon = { Icon(asset = Icons.TwoTone.CompareArrows) },
        onClick = onClick
    )
}

@Composable
private fun PokemonList(
    entries: List<PokemonEntry?>,
    editPokemonByIndex: (index: Int) -> Unit,
    deletePokemonByIndex: (Int) -> Unit,
) {
    val lastEntriesIndex = entries.size - 1
    LazyColumnForIndexed(items = entries) { pokemonIndex, entry: PokemonEntry? ->
        if (pokemonIndex == 0) {
            // TODO: BUG with lazy column contentPadding
            Spacer(Modifier.height(AppBarHeight))
        }
        PokemonEntryMenu(
            isPokemonEmpty = entry == null,
            edit = { editPokemonByIndex(pokemonIndex) },
            delete = { deletePokemonByIndex(pokemonIndex) }
        ) {
            PokemonEntry(entry)
        }
        if (pokemonIndex == lastEntriesIndex) {
            // TODO: BUG with lazy column contentPadding
            // fab: height + padding
            Spacer(Modifier.height(72.dp))
        } else {
            Divider()
        }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun PokemonEntry(entry: PokemonEntry?) {
    if (entry == null) {
        ListItemWithSprite(
            primaryText = "Empty Slot",
            spriteSource = SpriteSource.PokeBall,
            enabled = false
        )
    } else {
        ListItemWithSprite(
            primaryText = entry.nickname,
            secondaryText = entry.label,
            spriteSource = entry.source
        )
    }
}

@Composable
private fun PokemonEntryMenu(
    isPokemonEmpty: Boolean,
    edit: () -> Unit,
    delete: () -> Unit,
    pokemonEntry: @Composable () -> Unit,
) {
    var showOptions by rememberMutableState { false }
    if (showOptions) {
        ThemedDialog(onDismissRequest = { showOptions = false }) {
            Column {
                ListItem(
                    icon = { Icon(asset = Icons.TwoTone.Edit) },
                    text = { Text(text = "Edit") },
                    modifier = Modifier.clickable(onClick = { edit(); showOptions = false })
                )
                ListItem(
                    icon = { Icon(Icons.TwoTone.DeleteForever) },
                    text = { Text("Delete") },
                    modifier = Modifier.clickable(onClick = { delete(); showOptions = false })
                )
            }
        }
    }
    Box(
        modifier = Modifier.clickable(onClick = {
            if (isPokemonEmpty) edit() else showOptions = true
        }),
        children = pokemonEntry
    )
}
