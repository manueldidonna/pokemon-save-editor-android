package com.manueldidonna.redhex.home

import androidx.compose.*
import androidx.ui.core.Modifier
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.Column
import androidx.ui.layout.padding
import androidx.ui.material.Divider
import androidx.ui.material.ripple.ripple
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.Box
import com.manueldidonna.pk.core.ObservableBox
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.redhex.common.DialogItem
import com.manueldidonna.redhex.common.DialogMenu
import com.manueldidonna.redhex.dividerColor

@Model
private data class HomeState(
    var currentBoxIndex: Int,
    var movingPokemonPosition: Pokemon.Position? = null,
    var selectedPokemonIndex: Int = -1
)

@Composable
fun HomeScreen(saveData: SaveData) {
    val state = remember { HomeState(currentBoxIndex = saveData.currentBoxIndex) }

    val currentBox by stateFor(state.currentBoxIndex) {
        ObservableBox(saveData.getWriteableBox(state.currentBoxIndex))
    }

    val pokemonPreviews = stateFor(currentBox) { getPokemonPreviews(currentBox) }

    currentBox.onChange = {
        pokemonPreviews.value = getPokemonPreviews(currentBox)
    }

    VerticalScroller {
        Column {
            BoxHeader(
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
                boxName = currentBox.name,
                onBack = {
                    saveData.currentBoxIndex--
                    state.currentBoxIndex--
                },
                onForward = {
                    saveData.currentBoxIndex++
                    state.currentBoxIndex++
                }
            )
            PokemonList(pokemon = pokemonPreviews.value) { slot ->
                state.selectedPokemonIndex = slot
            }
        }
    }
    if (state.selectedPokemonIndex != -1) {
        ContextualActions(
            isPokemonEmpty = currentBox.getPokemon(state.selectedPokemonIndex).nickname.isEmpty(),
            dismiss = { state.selectedPokemonIndex = -1 },
            movePokemon = {
                state.movingPokemonPosition =
                    Pokemon.Position(state.currentBoxIndex, state.selectedPokemonIndex)
            },
            deletePokemon = {
                currentBox.deletePokemon(state.selectedPokemonIndex)
            }
        )
    }

}

private fun getPokemonPreviews(box: Box): List<PokemonPreview> {
    return List(box.pokemonCounts) { i ->
        box.getPokemon(i)
            .run { PokemonPreview(position.slot, nickname.ifEmpty { "Empty slot" }, level) }
    }
}

@Composable
private fun PokemonList(pokemon: List<PokemonPreview>, onSelection: (slot: Int) -> Unit) {
    pokemon.forEach {
        Clickable(onClick = { onSelection(it.slot) }, modifier = Modifier.ripple()) {
            PokemonCard(name = it.nickname, level = it.level)
        }
        Divider(color = dividerColor())
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
