package com.manueldidonna.redhex.details

import androidx.compose.*
import androidx.compose.frames.modelListOf
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.layout.*
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.Add
import androidx.ui.material.icons.twotone.Remove
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.ui.DialogItem
import com.manueldidonna.redhex.common.ui.DialogMenu

@Composable
fun PokemonMoves(pokemon: MutablePokemon) {
    val resources = PokemonResourcesAmbient.current.moves
    var selectedMove by state { -1 }
    val moves = remember {
        val moves = pokemon.moves
        modelListOf(
            resources.getMoveById(moves.getId(0)) to moves.getPowerPoints(0),
            resources.getMoveById(moves.getId(1)) to moves.getPowerPoints(1),
            resources.getMoveById(moves.getId(2)) to moves.getPowerPoints(2),
            resources.getMoveById(moves.getId(3)) to moves.getPowerPoints(3)
        )
    }

    fun updatePowerPointUps(index: Int, valueToApply: Int) {
        pokemon.mutator
            .movePowerPointUps(
                moveIndex = index,
                moveId = pokemon.moves.getId(index),
                ups = pokemon.moves.getUps(index) + valueToApply
            )
        moves[index] = moves[index].copy(second = pokemon.moves.getPowerPoints(index))
    }

    for (i in 0 until 4) {
        MoveDetails(
            moveName = moves[i].first,
            points = moves[i].second,
            upsCount = pokemon.moves.getUps(i),
            onClick = { selectedMove = i },
            increasePoints = { updatePowerPointUps(i, 1) },
            decreasePoints = { updatePowerPointUps(i, -1) }
        )
    }

    if (selectedMove >= 0) {
        MovesChooser(dismiss = { selectedMove = -1 }, resources = resources) { moveId ->
            pokemon.mutator
                .moveId(id = moveId, moveIndex = selectedMove)
                .movePowerPointUps(moveIndex = selectedMove, ups = 0, moveId = moveId)
            moves[selectedMove] =
                resources.getMoveById(moveId) to pokemon.moves.getPowerPoints(selectedMove)
        }
    }
}

@Composable
private fun MoveDetails(
    moveName: String,
    points: Int,
    upsCount: Int,
    onClick: () -> Unit,
    increasePoints: () -> Unit,
    decreasePoints: () -> Unit
) {
    val emphasis = EmphasisAmbient.current.run { if (moveName.isNotEmpty()) high else disabled }
    Row(
        verticalGravity = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .preferredHeight(56.dp)
            .clickable(onClick = onClick)
            .padding(start = RowStartPadding, end = RowEndPadding)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = moveName.ifEmpty { "None" },
            style = MaterialTheme.typography.body1,
            color = emphasis.applyEmphasis(MaterialTheme.colors.onSurface)
        )
        if (moveName.isNotEmpty())
            PowerPointsCounter(
                points = points,
                increase = if (upsCount >= 3) null else increasePoints,
                decrease = if (upsCount <= 0) null else decreasePoints
            )
    }
}

@Composable
private fun PowerPointsCounter(points: Int, increase: (() -> Unit)?, decrease: (() -> Unit)?) {
    Row(
        verticalGravity = Alignment.CenterVertically,
        modifier = Modifier.preferredWidth(CounterWidth)
    ) {
        CounterButton(icon = Icons.TwoTone.Remove, onClick = decrease)
        Text(
            text = points.toString(),
            modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.body2,
            color = EmphasisAmbient.current.high.applyEmphasis(MaterialTheme.colors.onSurface)
        )
        CounterButton(icon = Icons.TwoTone.Add, onClick = increase)
    }
}

@Composable
private fun CounterButton(icon: VectorAsset, onClick: (() -> Unit)?) {
    val emphasis = EmphasisAmbient.current.run { if (onClick != null) high else disabled }
    if (onClick != null) {
        IconButton(onClick = onClick) {
            Icon(asset = icon, tint = emphasis.applyEmphasis(MaterialTheme.colors.onSurface))
        }
    } else {
        Box(modifier = Modifier.preferredSize(48.dp), gravity = ContentGravity.Center) {
            Icon(asset = icon, tint = emphasis.applyEmphasis(MaterialTheme.colors.onSurface))
        }
    }
}

@Composable
private inline fun MovesChooser(
    noinline dismiss: () -> Unit,
    resources: PokemonTextResources.Moves,
    crossinline onMoveSelected: (id: Int) -> Unit
) {
    DialogMenu(dismiss = dismiss) {
        AdapterList(data = resources.getAllMoves()
            .mapIndexed { index, s -> Pair(index, s) }
            .sortedBy { it.second }
        ) {
            DialogItem(text = it.second, onClick = { onMoveSelected(it.first) })
        }
    }
}

private val CounterWidth = 140.dp

private val RowEndPadding = 16.dp

private val RowStartPadding = 24.dp
