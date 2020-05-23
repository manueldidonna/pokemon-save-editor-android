package com.manueldidonna.redhex.home

import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.getValue
import androidx.compose.stateFor
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.zIndex
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.*
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.ChevronLeft
import androidx.ui.material.icons.twotone.ChevronRight
import androidx.ui.material.ripple.ripple
import androidx.ui.savedinstancestate.Saver
import androidx.ui.savedinstancestate.SaverScope
import androidx.ui.savedinstancestate.rememberSavedInstanceState
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpritesRetrieverAmbient
import com.manueldidonna.redhex.common.pokemon.PokemonSpritesRetriever
import com.manueldidonna.redhex.common.ui.DialogItem
import com.manueldidonna.redhex.common.ui.DialogMenu
import com.manueldidonna.redhex.common.ui.ToolbarHeight
import com.manueldidonna.redhex.common.ui.TranslucentToolbar
import com.manueldidonna.redhex.home.HomeAction.*
import java.io.File

@Model
private data class HomeState(
    var currentStorageIndex: StorageIndex,
    var movingPokemonPosition: Pokemon.Position? = null,
    var selectedPokemonIndex: Int = -1
)

private sealed class HomeAction {
    object IncreaseBoxIndex : HomeAction()
    object DecreaseBoxIndex : HomeAction()
    data class DeleteSlot(val slot: Int) : HomeAction()
    data class SwapPokemon(val first: Pokemon.Position, val second: Pokemon.Position) : HomeAction()
}

interface HomeEvents {
    fun showPokemonDetails(position: Pokemon.Position)
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, saveData: SaveData, listener: HomeEvents) {
    val pokemonResources = PokemonResourcesAmbient.current
    val spritesRetriever = PokemonSpritesRetrieverAmbient.current

    val state = rememberSavedInstanceState(saver = object : Saver<HomeState, Int> {
        override fun restore(value: Int): HomeState? {
            return HomeState(
                currentStorageIndex = StorageIndex.Box(value, saveData.currentBoxIndex == value)
            )
        }

        override fun SaverScope.save(value: HomeState): Int? {
            return value.currentStorageIndex.value
        }
    }) {
        HomeState(currentStorageIndex = StorageIndex.Box(saveData.currentBoxIndex, true))
    }

    val currentStorage by stateFor(state.currentStorageIndex.value) {
        saveData.getMutableStorage(state.currentStorageIndex)
    }

    val pokemonPreviews = stateFor(currentStorage) {
        getPokemonPreviews(currentStorage, pokemonResources, spritesRetriever)
    }

    fun executeAction(action: HomeAction) {
        when (action) {
            IncreaseBoxIndex, DecreaseBoxIndex -> {
                val sumValue = if (action == IncreaseBoxIndex) 1 else -1
                val wasParty = state.currentStorageIndex.isParty
                val newIndex = state.currentStorageIndex.value + sumValue
                val maxBoxIndex = saveData.boxCounts - 1
                state.currentStorageIndex = when {
                    newIndex in 0..maxBoxIndex -> {
                        saveData.currentBoxIndex = newIndex
                        StorageIndex.Box(newIndex, isCurrentBox = true)
                    }
                    wasParty -> {
                        StorageIndex.Box(if (sumValue == 1) 0 else maxBoxIndex, isCurrentBox = true)
                    }
                    else -> StorageIndex.Party
                }
            }
            is DeleteSlot -> {
                currentStorage.deletePokemon(action.slot)
                pokemonPreviews.value =
                    getPokemonPreviews(currentStorage, pokemonResources, spritesRetriever)
            }
            is SwapPokemon -> {
                saveData.swapPokemon(action.first, action.second)
                pokemonPreviews.value =
                    getPokemonPreviews(currentStorage, pokemonResources, spritesRetriever)
            }
        }
    }

    Stack(modifier = modifier) {
        VerticalScroller {
            Column {
                Spacer(modifier = Modifier.preferredHeight(56.dp))
                PokemonList(pokemon = pokemonPreviews.value) { slot ->
                    state.selectedPokemonIndex = slot
                }
                Spacer(modifier = Modifier.preferredHeight(16.dp))
            }
        }
        HomeToolbar(
            // TODO: there is a bug with zIndex. Check again in dev-12
            // It doesn't receive cliks if positioned before the views that it overlaps
            modifier = Modifier.preferredHeight(ToolbarHeight).zIndex(8f).gravity(Alignment.TopCenter),
            title = currentStorage.name,
            onBack = { executeAction(DecreaseBoxIndex) },
            onForward = { executeAction(IncreaseBoxIndex) }
        )
    }

    if (state.selectedPokemonIndex != -1) {
        ContextualActions(
            dismiss = { state.selectedPokemonIndex = -1 },
            movePokemon = {
                val position = Pokemon.Position(
                    index = state.currentStorageIndex,
                    slot = state.selectedPokemonIndex
                )
                if (state.movingPokemonPosition != null) {
                    executeAction(SwapPokemon(state.movingPokemonPosition!!, position))
                    state.movingPokemonPosition = null
                } else {
                    state.movingPokemonPosition = position
                }
            },
            deletePokemon = { executeAction(DeleteSlot(state.selectedPokemonIndex)) },
            viewPokemon = {
                listener.showPokemonDetails(
                    Pokemon.Position(
                        state.currentStorageIndex,
                        state.selectedPokemonIndex
                    )
                )
            }
        )
    }
}

private fun getPokemonPreviews(
    storage: Storage,
    resources: PokemonTextResources,
    spritesRetriever: PokemonSpritesRetriever
): List<PokemonPreview?> {
    return List(storage.pokemonCounts) { i ->
        storage.getPokemon(i).run {
            if (isEmpty) null else {
                PokemonPreview(
                    nickname = nickname,
                    labels = listOf("L. $level", resources.natures.getNatureById(natureId)),
                    sprite = File(spritesRetriever.getSpritesPathFromId(speciesId))
                )
            }
        }
    }
}

@Composable
private fun HomeToolbar(
    modifier: Modifier = Modifier,
    title: String,
    onBack: () -> Unit,
    onForward: () -> Unit
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
private fun PokemonList(pokemon: List<PokemonPreview?>, onSelection: (slot: Int) -> Unit) {
    pokemon.forEachIndexed { index, pk ->
        Clickable(onClick = { onSelection(index) }, modifier = Modifier.ripple()) {
            PokemonCard(preview = pk)
        }
    }
}

@Composable
private fun ContextualActions(
    dismiss: () -> Unit,
    movePokemon: () -> Unit,
    deletePokemon: () -> Unit,
    viewPokemon: () -> Unit
) {
    DialogMenu(dismiss = dismiss) {
        DialogItem(text = "View pokemon", onClick = viewPokemon)
        DialogItem(text = "Delete pokemon", onClick = deletePokemon)
        DialogItem(text = "Move pokemon", onClick = movePokemon)
    }
}
