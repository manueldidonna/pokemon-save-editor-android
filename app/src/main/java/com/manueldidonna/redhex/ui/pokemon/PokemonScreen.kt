package com.manueldidonna.redhex.ui.pokemon

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import com.manueldidonna.pk.core.StorageSystem
import com.manueldidonna.redhex.common.SurfaceDialog
import com.manueldidonna.redhex.common.rememberMutableState

@Composable
fun PokemonScreen(
    storageSystem: StorageSystem,
    showPokedex: () -> Unit
) {
    var storageIndex by savedInstanceState { storageSystem.storageIndices.first }

    val entities by storageSystem.getPokemonEntitiesAsState(storageIndex)

    var showStoragesList by rememberMutableState(init = { false })

    if (showStoragesList)
        SurfaceDialog(onDismissRequest = { showStoragesList = false }) {
            StoragesList(
                storageSystem = storageSystem,
                onStorageClick = { index ->
                    storageIndex = index
                    showStoragesList = false
                }
            )
        }

    Column {
        PokemonAppBar(
            storageName = storageSystem[storageIndex].name,
            showStorageList = { showStoragesList = true },
            showPokedex = showPokedex,
            nextStorage = {
                storageIndex = storageIndex.increaseIn(storageSystem.storageIndices)
            },
            previousStorage = {
                storageIndex = storageIndex.decreaseIn(storageSystem.storageIndices)
            }
        )
        PokemonList(pokemonEntities = entities, onEntityClick = { /*TODO*/ })
    }
}

private fun Int.increaseIn(indices: IntRange): Int {
    return if (this + 1 <= indices.last) return this + 1 else indices.first
}

private fun Int.decreaseIn(indices: IntRange): Int {
    return if (this - 1 < indices.first) return indices.last else this - 1
}