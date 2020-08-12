package com.manueldidonna.redhex.details

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.ui.tooling.preview.Preview
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.ui.LightColors
import com.manueldidonna.redhex.common.ui.ThemedDialog

@Composable
fun PokemonMoves(pokemon: MutablePokemon) {
    val resources = PokemonResourcesAmbient.current.moves

    val moves = remember {
        mutableStateListOf(
            pokemon.selectMove(0, Pokemon::Move),
            pokemon.selectMove(1, Pokemon::Move),
            pokemon.selectMove(2, Pokemon::Move),
            pokemon.selectMove(3, Pokemon::Move)
        )
    }

    val selectedMoveIndex = remember { mutableStateOf(-1) }

    for (i in 0 until 4) {
        val move = moves[i]
        PokemonMove(
            name = resources.getMoveById(move.id),
            powerPoints = move.powerPoints,
            ups = move.ups,
            onClick = { selectedMoveIndex.value = i },
            maximizeUps = {
                pokemon.mutator.move(i, Pokemon.Move(id = move.id, powerPoints = 999, ups = 3))
                moves[i] = pokemon.selectMove(i, Pokemon::Move)
            }
        )
    }

    if (selectedMoveIndex.value >= 0) {
        MoveChooser(
            moves = resources.getAllMoves(pokemon.version),
            onMoveSelected = { move ->
                if (move != null) {
                    val availableIndex = pokemon.getEmptyMoveIndexOrElse(selectedMoveIndex.value)
                    pokemon.mutator.move(availableIndex, move)
                    moves[availableIndex] = pokemon.selectMove(availableIndex, Pokemon::Move)
                }
                selectedMoveIndex.value = -1
            }
        )
    }
}

private fun Pokemon.getEmptyMoveIndexOrElse(defaultIndex: Int): Int {
    val isDefaultIndexEmpty = selectMove(defaultIndex) { id, _, _ ->
        id == Pokemon.Move.Empty.id
    }

    if (isDefaultIndexEmpty) {
        for (i in 0 until 4) {
            val isEmpty = selectMove(i) { id, _, _ -> id == Pokemon.Move.Empty.id }
            if (isEmpty) return i
        }
    }
    return defaultIndex
}

@Composable
private fun PokemonMove(
    name: String,
    powerPoints: Int,
    ups: Int,
    onClick: () -> Unit,
    maximizeUps: () -> Unit
) {
    val emptyColor = EmphasisAmbient.current.disabled.applyEmphasis(MaterialTheme.colors.onSurface)
    if (name.isEmpty()) {
        ListItem(
            text = { Text(text = "None", color = emptyColor) },
            onClick = onClick
        )
    } else {
        var maximizeButton: @Composable (() -> Unit)? = null
        if (ups < 3) {
            maximizeButton = @Composable {
                TextButton(onClick = maximizeUps) {
                    Text(text = "MAX PP-UPS")
                }
            }
        }
        ListItem(
            text = { Text(text = name) },
            secondaryText = { Text(text = "$powerPoints PP - $ups PP-ups") },
            onClick = onClick,
            trailing = maximizeButton
        )
    }
}

@Composable
private fun MoveChooser(
    moves: List<String>,
    onMoveSelected: (Pokemon.Move?) -> Unit
) {
    ThemedDialog(onCloseRequest = { onMoveSelected(null) }) {
        LazyColumnFor(items = moves.sorted()) {
            ListItem(
                text = it,
                onClick = {
                    val id = moves.indexOf(it)
                    val move = when (id) {
                        -1 -> null
                        0 -> Pokemon.Move.Empty
                        else -> Pokemon.Move(id = id, powerPoints = 999, ups = 0)
                    }
                    onMoveSelected(move)
                }
            )
        }
    }
}

@Preview
@Composable
private fun PreviewPokemonMove() {
    MaterialTheme(colors = LightColors) {
        Column {
            PokemonMove(name = "", powerPoints = 0, ups = 2, onClick = {}, maximizeUps = {})
            PokemonMove(name = "Azione", powerPoints = 34, ups = 2, onClick = {}, maximizeUps = {})
            PokemonMove(name = "Azione", powerPoints = 34, ups = 3, onClick = {}, maximizeUps = {})
        }
    }
}
