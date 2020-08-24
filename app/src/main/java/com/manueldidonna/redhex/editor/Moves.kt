package com.manueldidonna.redhex.editor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AddCircle
import androidx.compose.material.icons.twotone.RemoveCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.Version
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.rememberMutableState
import com.manueldidonna.redhex.common.ThemedDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun ModifyMoves(
    version: Version,
    moves: List<ObservablePokemon.Move>,
    onMoveChange: (index: Int, move: Pokemon.Move) -> Unit,
) {
    var chosenMoveIndex by rememberMutableState { -1 }
    if (chosenMoveIndex >= 0) {
        ChangeMoveDialog(
            version = version,
            onCloseRequest = { chosenMoveIndex = -1 },
            onMoveChange = { onMoveChange(chosenMoveIndex, it) }
        )
    }
    Column {
        Text(
            text = "Current Moves",
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        for (index in 0 until 4) {
            val move = moves[index]
            Move(
                name = moveNameById(move.id),
                powerPoints = move.powerPoints,
                ups = move.ups,
                onClick = { chosenMoveIndex = index },
                increaseUps = {
                    onMoveChange(index, move.copy(ups = move.ups + 1, powerPoints = 999))
                },
                decreaseUps = {
                    onMoveChange(index, move.copy(ups = move.ups - 1, powerPoints = 999))
                }
            )
        }
    }
}

@Composable
private fun Move(
    name: String,
    powerPoints: Int,
    ups: Int,
    onClick: () -> Unit,
    increaseUps: () -> Unit,
    decreaseUps: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .preferredHeight(48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalGravity = Alignment.CenterVertically
    ) {
        if (name.isEmpty()) {
            Text(text = "None", color = emptyMoveColor(), style = MaterialTheme.typography.body1)
        } else {
            Text(
                text = name,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$powerPoints PP",
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.secondary
            )
            Spacer(Modifier.width(16.dp))
            IconButton(
                enabled = ups > 0,
                asset = Icons.TwoTone.RemoveCircle,
                onClick = decreaseUps
            )
            IconButton(
                enabled = ups < 3,
                asset = Icons.TwoTone.AddCircle,
                onClick = increaseUps
            )
        }
    }
}

@Stable
@Composable
private fun moveNameById(id: Int): String {
    val resources = PokemonResourcesAmbient.current.moves
    return resources.getMoveById(id)
}

@Stable
@Composable
private fun emptyMoveColor(): Color {
    val emphasis = EmphasisAmbient.current.disabled
    return emphasis.applyEmphasis(MaterialTheme.colors.onSurface)
}

@Composable
private fun IconButton(enabled: Boolean, asset: VectorAsset, onClick: () -> Unit) {
    if (enabled) {
        IconButton(onClick = onClick) {
            Icon(asset = asset, tint = MaterialTheme.colors.secondary)
        }
    } else {
        Box(modifier = Modifier.preferredSize(48.dp), gravity = ContentGravity.Center) {
            Icon(asset = asset, tint = emptyMoveColor())
        }
    }
}

@Immutable
private data class MoveEntity(val id: Int, val name: String)

@Composable
private fun ChangeMoveDialog(
    version: Version,
    onCloseRequest: () -> Unit,
    onMoveChange: (Pokemon.Move) -> Unit,
) {
    val resources = PokemonResourcesAmbient.current.moves
    val (moves, updateMoves) = rememberMutableState<List<MoveEntity>> { emptyList() }
    launchInComposition {
        withContext(Dispatchers.IO) {
            updateMoves(resources
                .getAllMoves(version)
                .mapIndexed { index, name -> MoveEntity(index, name) }
                .sortedBy { it.name }
            )
        }
    }
    ThemedDialog(onDismissRequest = onCloseRequest) {
        LazyColumnFor(items = moves) { entity ->
            ListItem(
                text = { Text(text = entity.name) },
                modifier = Modifier.clickable(onClick = {
                    val move =
                        if (entity.id == 0) Pokemon.Move.Empty
                        else Pokemon.Move.Immutable(id = entity.id, powerPoints = 999, ups = 0)
                    onMoveChange(move)
                    onCloseRequest()
                })
            )
        }
    }
}
