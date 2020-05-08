package com.manueldidonna.redhex.home

import androidx.compose.*
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
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.Box
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.core.setCoercedBoxIndex
import com.manueldidonna.pk.resources.PokemonResources
import com.manueldidonna.redhex.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.DialogItem
import com.manueldidonna.redhex.common.DialogMenu
import com.manueldidonna.redhex.dividerColor
import com.manueldidonna.redhex.home.HomeAction.*
import com.manueldidonna.redhex.translucentSurfaceColor

@Model
private data class HomeState(
    var currentBoxIndex: Int,
    var movingPokemonPosition: Pokemon.Position? = null,
    var selectedPokemonIndex: Int = -1
)

private sealed class HomeAction {
    object IncreaseBoxIndex : HomeAction()
    object DecreaseBoxIndex : HomeAction()
    data class DeleteSlot(val slot: Int) : HomeAction()
}

@Composable
fun HomeScreen(saveData: SaveData) {
    val pokemonResources = PokemonResourcesAmbient.current
    val state = remember { HomeState(currentBoxIndex = saveData.currentBoxIndex) }

    val currentBox by stateFor(state.currentBoxIndex) {
        saveData.getWriteableBox(state.currentBoxIndex)
    }

    val pokemonPreviews = stateFor(currentBox) { getPokemonPreviews(currentBox, pokemonResources) }

    fun executeAction(action: HomeAction) {
        when (action) {
            IncreaseBoxIndex -> {
                state.currentBoxIndex = saveData.setCoercedBoxIndex(saveData.currentBoxIndex + 1)
            }
            DecreaseBoxIndex -> {
                state.currentBoxIndex = saveData.setCoercedBoxIndex(saveData.currentBoxIndex - 1)
            }
            is DeleteSlot -> {
                currentBox.deletePokemon(action.slot)
                pokemonPreviews.value = getPokemonPreviews(currentBox, pokemonResources)
            }
        }
    }

    Box {
        VerticalScroller {
            Column {
                Spacer(modifier = Modifier.preferredHeight(64.dp))
                PokemonList(pokemon = pokemonPreviews.value) { slot ->
                    state.selectedPokemonIndex = slot
                }
                Spacer(modifier = Modifier.preferredHeight(16.dp))
            }
        }
        HomeToolbar(
            modifier = Modifier.preferredHeight(56.dp),
            title = currentBox.name,
            onBack = { executeAction(DecreaseBoxIndex) },
            onForward = { executeAction(IncreaseBoxIndex) }
        )
    }

    if (state.selectedPokemonIndex != -1) {
        ContextualActions(
            isPokemonEmpty = currentBox.getPokemon(state.selectedPokemonIndex).nickname.isEmpty(),
            dismiss = { state.selectedPokemonIndex = -1 },
            movePokemon = {
                state.movingPokemonPosition =
                    Pokemon.Position(state.currentBoxIndex, state.selectedPokemonIndex)
            },
            deletePokemon = { executeAction(DeleteSlot(state.selectedPokemonIndex)) }
        )
    }
}

private fun getPokemonPreviews(box: Box, resources: PokemonResources): List<PokemonPreview> {
    return List(box.pokemonCounts) { i ->
        box.getPokemon(i).run {
            PokemonPreview(
                slot = position.slot,
                nickname = nickname,
                labels = listOf("L. $level", resources.natures.getNatureById(natureId))
            )
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
        modifier = modifier.zIndex(8f).drawBackground(color = translucentSurfaceColor()),
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
                    .preferredWidthIn(minWidth = 140.dp)
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
private fun PokemonList(pokemon: List<PokemonPreview>, onSelection: (slot: Int) -> Unit) {
    pokemon.forEach {
        Clickable(onClick = { onSelection(it.slot) }, modifier = Modifier.ripple()) {
            PokemonCard(name = it.nickname, labels = it.labels)
        }
    }
}

@Composable
private fun ContextualActions(
    isPokemonEmpty: Boolean,
    dismiss: () -> Unit,
    movePokemon: () -> Unit,
    deletePokemon: () -> Unit
) {
    DialogMenu(dismiss = dismiss) {
        if (!isPokemonEmpty)
            DialogItem(text = "Delete pokemon", onClick = deletePokemon)
        DialogItem(text = "Move pokemon", onClick = movePokemon)
    }
}
