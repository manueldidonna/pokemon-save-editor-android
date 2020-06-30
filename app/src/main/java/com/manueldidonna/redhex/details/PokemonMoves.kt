package com.manueldidonna.redhex.details

import androidx.compose.*
import androidx.compose.frames.modelListOf
import androidx.ui.foundation.Text
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.layout.Column
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.ListItem
import androidx.ui.material.MaterialTheme
import androidx.ui.material.TextButton
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
        modelListOf(
            pokemon.selectMove(0, Pokemon::Move),
            pokemon.selectMove(1, Pokemon::Move),
            pokemon.selectMove(2, Pokemon::Move),
            pokemon.selectMove(3, Pokemon::Move)
        )
    }

    var selectedMoveIndex by state { -1 }

    for (i in 0 until 4) {
        val move = moves[i]
        PokemonMove(
            name = resources.getMoveById(move.id),
            powerPoints = move.powerPoints,
            ups = move.ups,
            onClick = { selectedMoveIndex = i },
            maximizeUps = {
                pokemon.mutator.move(i, Pokemon.Move.maxPowerPoints(move.id))
                moves[i] = pokemon.selectMove(i, Pokemon::Move)
            }
        )
    }

    if (selectedMoveIndex >= 0) {
        MoveChooser(
            moves = resources.getAllMoves(),
            onMoveSelected = { move ->
                if (move != null) {
                    val availableIndex = pokemon.getEmptyMoveIndexOrElse(selectedMoveIndex)
                    pokemon.mutator.move(availableIndex, move)
                    moves[availableIndex] = pokemon.selectMove(availableIndex, Pokemon::Move)
                }
                selectedMoveIndex = -1
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
        LazyColumnItems(items = moves.sorted()) {
            ListItem(
                text = it,
                onClick = {
                    val id = moves.indexOf(it)
                    val move = when (id) {
                        -1 -> null
                        0 -> Pokemon.Move.Empty
                        else -> Pokemon.Move.maxPowerPoints(id, ups = 0)
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
