package com.manueldidonna.redhex.details

import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.clip
import androidx.ui.foundation.AdapterList
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.Text
import androidx.ui.foundation.drawBackground
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.Row
import androidx.ui.layout.padding
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ripple.ripple
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.ui.DialogItem
import com.manueldidonna.redhex.common.ui.DialogMenu

@Composable
fun PokemonMoves(pokemon: MutablePokemon) {
    val resources = PokemonResourcesAmbient.current.moves
    var selectedMove by state { -1 }
    Text(
        modifier = Modifier.padding(top = 24.dp, bottom = 16.dp, start = 24.dp),
        text = "Moves",
        color = MaterialTheme.colors.onSurface,
        style = MaterialTheme.typography.h6
    )
    for (i in 0 until 4) {
        Clickable(onClick = { selectedMove = i }, modifier = Modifier.ripple()) {
            MoveDetail(
                moveName = resources.getMoveById(pokemon.moves.getId(i)),
                points = pokemon.moves.getPowerPoints(i)
            )
        }
    }
    if (selectedMove >= 0) {
        DialogMenu(dismiss = { selectedMove = -1 }) {
            AdapterList(
                data = resources.getAllMoves()
                    .mapIndexed { index, s -> Pair(index, s) }
                    .sortedBy { it.second }
            ) {
                DialogItem(
                    text = it.second,
                    onClick = { pokemon.mutator.moveId(it.first, selectedMove) }
                )
            }
        }
    }
}

@Composable
private fun MoveDetail(moveName: String, points: Int) {
    val emphasis = EmphasisAmbient.current.run { if (moveName.isNotEmpty()) high else medium }
    Row(
        verticalGravity = Alignment.CenterVertically,
        modifier = Modifier
            .padding(top = 8.dp, bottom = 8.dp, start = 24.dp, end = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .drawBackground(color = emphasis.emphasize(MaterialTheme.colors.secondaryVariant))
    ) {
        Text(
            modifier = Modifier.weight(1f).padding(16.dp),
            text = moveName.ifEmpty { "None" },
            style = MaterialTheme.typography.body1,
            color = emphasis.emphasize(MaterialTheme.colors.onSecondary)
        )
        if (moveName.isNotEmpty())
            Text(
                text = "$points PP",
                modifier = Modifier.padding(end = 16.dp),
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.onSecondary
            )
    }
}