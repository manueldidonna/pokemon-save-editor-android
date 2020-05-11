package com.manueldidonna.redhex.home

import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.getValue
import androidx.compose.stateFor
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.zIndex
import androidx.ui.foundation.*
import androidx.ui.layout.*
import androidx.ui.material.Divider
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
import com.manueldidonna.pk.resources.PokemonResources
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpritesRetrieverAmbient
import com.manueldidonna.redhex.common.pokemon.PokemonSpritesRetriever
import com.manueldidonna.redhex.common.ui.DialogItem
import com.manueldidonna.redhex.common.ui.DialogMenu
import com.manueldidonna.redhex.dividerColor
import com.manueldidonna.redhex.home.HomeAction.*
import com.manueldidonna.redhex.translucentSurfaceColor
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
            return HomeState(currentStorageIndex = StorageIndex(value))
        }

        override fun SaverScope.save(value: HomeState): Int? {
            return value.currentStorageIndex.value
        }
    }) {
        HomeState(currentStorageIndex = StorageIndex(saveData.currentBoxIndex))
    }

    val currentStorage by stateFor(state.currentStorageIndex.value) {
        saveData.getMutableStorage(state.currentStorageIndex)
    }

    val pokemonPreviews = stateFor(currentStorage) {
        getPokemonPreviews(currentStorage, pokemonResources, spritesRetriever)
    }

    fun executeAction(action: HomeAction) {
        when (action) {
            IncreaseBoxIndex -> {
                val newIndex = state.currentStorageIndex.nextIndex(saveData.boxCounts)
                if (!newIndex.isParty)
                    saveData.currentBoxIndex = newIndex.value
                state.currentStorageIndex = newIndex
            }
            DecreaseBoxIndex -> {
                val newIndex = state.currentStorageIndex.previousIndex(saveData.boxCounts)
                if (!newIndex.isParty)
                    saveData.currentBoxIndex = newIndex.value
                state.currentStorageIndex = newIndex
            }
            is DeleteSlot -> {
                currentStorage.deletePokemon(action.slot)
                pokemonPreviews.value =
                    getPokemonPreviews(currentStorage, pokemonResources, spritesRetriever)
            }
        }
    }

    Box(modifier = modifier) {
        VerticalScroller {
            Column {
                Spacer(modifier = Modifier.preferredHeight(72.dp))
                PokemonList(pokemon = pokemonPreviews.value) { slot ->
                    state.selectedPokemonIndex = slot
                }
                Spacer(modifier = Modifier.preferredHeight(16.dp))
            }
        }
        HomeToolbar(
            // TODO: there is a bug with zIndex. Check again in dev-12
            // It doesn't receive cliks if positioned before the views that it overlaps
            modifier = Modifier.preferredHeight(56.dp).zIndex(8f),
            title = currentStorage.name,
            onBack = { executeAction(DecreaseBoxIndex) },
            onForward = { executeAction(IncreaseBoxIndex) }
        )
    }

    if (state.selectedPokemonIndex != -1) {
        ContextualActions(
            isPokemonEmpty = currentStorage.getPokemon(state.selectedPokemonIndex).nickname.isEmpty(),
            dismiss = { state.selectedPokemonIndex = -1 },
            movePokemon = {
                state.movingPokemonPosition = Pokemon.Position(
                    index = state.currentStorageIndex,
                    slot = state.selectedPokemonIndex
                )
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
    resources: PokemonResources,
    spritesRetriever: PokemonSpritesRetriever
): List<PokemonPreview?> {
    return List(storage.pokemonCounts) { i ->
        storage.getPokemon(i).run {
            // TODO: add isEmpty: Boolean to Pokemon
            if (nickname.isEmpty() || speciesId == 0) null else {
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
    Column(
        modifier = modifier.drawBackground(color = translucentSurfaceColor()),
        verticalArrangement = Arrangement.Center,
        horizontalGravity = Alignment.CenterHorizontally
    ) {
        Row(
            verticalGravity = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.TwoTone.ChevronLeft, tint = MaterialTheme.colors.primary)
            }
            Text(
                modifier = Modifier
                    .preferredWidthIn(minWidth = 150.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally),
                text = title,
                style = MaterialTheme.typography.h6.copy(color = MaterialTheme.colors.primary)
            )
            IconButton(onClick = onForward) {
                Icon(Icons.TwoTone.ChevronRight, tint = MaterialTheme.colors.primary)
            }
        }
        Divider(color = dividerColor())
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
    isPokemonEmpty: Boolean,
    dismiss: () -> Unit,
    movePokemon: () -> Unit,
    deletePokemon: () -> Unit,
    viewPokemon: () -> Unit
) {
    DialogMenu(dismiss = dismiss) {
        DialogItem(text = "View pokemon", onClick = viewPokemon)
        if (!isPokemonEmpty)
            DialogItem(text = "Delete pokemon", onClick = deletePokemon)
        DialogItem(text = "Move pokemon", onClick = movePokemon)
    }
}
