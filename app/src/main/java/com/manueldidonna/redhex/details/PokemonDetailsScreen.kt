package com.manueldidonna.redhex.details

import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.ArrowBack
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.redhex.dividerColor
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
            Column {
                Species(pokemon = pokemon)
                Experience(pokemon = pokemon)
                Divider(color = dividerColor())
                PokemonMoves(pokemon)
            }
        }
    }
}

@Composable
private fun Species(pokemon: MutablePokemon) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        backgroundColor = MaterialTheme.colors.primary,
        gravity = ContentGravity.Center,
        padding = 8.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = pokemon.nickname,
            color = MaterialTheme.colors.onPrimary,
            style = MaterialTheme.typography.body1
        )
    }
}


@Composable
private fun Experience(pokemon: MutablePokemon) {
    var level by state { pokemon.level.toFloat() }
    Row(
        verticalGravity = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
    ) {
        Text(
            text = "Level",
            modifier = Modifier.padding(end = 8.dp).preferredWidth(48.dp),
            style = MaterialTheme.typography.subtitle1
        )
        Text(
            text = level.roundToInt().toString(),
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
                .preferredWidth(24.dp)
                .wrapContentWidth(Alignment.CenterHorizontally),
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
