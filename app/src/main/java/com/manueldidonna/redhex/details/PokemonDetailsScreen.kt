package com.manueldidonna.redhex.details

import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.*
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Slider
import androidx.ui.material.TopAppBar
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.ArrowBack
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import kotlin.math.roundToInt

interface PokemonDetailsEvents {
    fun goBackToPokemonList()
}

@Composable
fun PokemonDetailsScreen(
    modifier: Modifier = Modifier,
    pokemon: MutablePokemon,
    listener: PokemonDetailsEvents
) {
    Column(modifier.fillMaxSize()) {
        TopAppBar(
            backgroundColor = MaterialTheme.colors.surface,
            navigationIcon = {
                IconButton(onClick = listener::goBackToPokemonList) {
                    Icon(Icons.TwoTone.ArrowBack)
                }
            },
            title = { Text(text = "Edit pokemon") }
        )
        VerticalScroller {
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp)) {
                Spacer(modifier = Modifier.preferredHeight(16.dp))
                Experience(pokemon = pokemon)
                Spacer(modifier = Modifier.preferredHeight(16.dp))
            }
        }
    }
}

@Composable
private fun Experience(pokemon: MutablePokemon) {
    var level by state { pokemon.level.toFloat() }
    Row(verticalGravity = Alignment.CenterVertically) {
        Text(
            text = "Level",
            modifier = Modifier.padding(end = 8.dp).preferredWidth(48.dp),
            style = MaterialTheme.typography.subtitle1
        )
        Text(
            text = level.roundToInt().toString(),
            modifier = Modifier.padding(start = 8.dp, end = 8.dp).preferredWidth(24.dp),
            style = MaterialTheme.typography.subtitle2
        )
        Slider(
            modifier = Modifier.weight(1f),
            value = level,
            onValueChange = { level = it },
            valueRange = 1f..100f,
            onValueChangeEnd = { pokemon.mutator.level(level.roundToInt()) }
        )
    }

}