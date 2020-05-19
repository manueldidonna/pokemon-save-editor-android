package com.manueldidonna.redhex.details

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.ArrowBack
import androidx.ui.material.ripple.ripple
import androidx.ui.text.font.FontWeight
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpritesRetrieverAmbient
import com.manueldidonna.redhex.common.pokemon.pokemonSpriteSize
import com.manueldidonna.redhex.common.ui.DialogItem
import com.manueldidonna.redhex.common.ui.DialogMenu
import com.manueldidonna.redhex.dividerColor
import dev.chrisbanes.accompanist.coil.CoilImage
import java.io.File
import kotlin.math.roundToInt

interface PokemonDetailsEvents {
    fun goBackToPokemonList()
}

@Composable
fun PokemonDetailsScreen(
    modifier: Modifier = Modifier,
    pokemon: MutablePokemon,
    pokedex: Pokedex,
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
                Species(pokemon = pokemon, pokedex = pokedex)
                Divider(color = dividerColor())
                Experience(pokemon = pokemon)
                Divider(color = dividerColor())
                PokemonMovesEditor(pokemon)
            }
        }
    }
}

@Composable
private fun Species(pokemon: MutablePokemon, pokedex: Pokedex) {
    val species = PokemonResourcesAmbient.current.species
    val spritesRetriever = PokemonSpritesRetrieverAmbient.current
    var speciesId by state { pokemon.speciesId }
    val spriteSource = remember(speciesId) {
        File(spritesRetriever.getSpritesPathFromId(speciesId))
    }

    var showSpeciesDialog by state { false }

    Clickable(onClick = { showSpeciesDialog = true }, modifier = Modifier.ripple()) {
        Row(
            verticalGravity = Alignment.CenterVertically,
            modifier = Modifier.preferredHeight(56.dp).fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.preferredWidth(8.dp))
            CoilImage(
                data = spriteSource,
                modifier = Modifier.pokemonSpriteSize()
            )
            Text(
                text = species.getSpeciesById(speciesId),
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

    if (showSpeciesDialog)
        DialogMenu(dismiss = { showSpeciesDialog = false }) {
            AdapterList(data = species.getAllSpecies()
                .drop(1) // TODO: manage empty species id
                .mapIndexed { index, s -> Pair(index, s) }
            ) {
                DialogItem(
                    text = it.second,
                    onClick = {
                        pokemon.mutator
                            .speciesId(it.first + 1)
                            .level(pokemon.level)
                            // TODO: uppercase name is a gen 1-3 detail. Abstract it
                            .nickname(it.second.toUpperCase())
                        pokedex.setEntry(Pokedex.Entry.owned(it.first + 1))
                        speciesId = pokemon.speciesId
                    }
                )
            }
        }
}


@Composable
private fun Experience(pokemon: MutablePokemon) {
    var level by state { pokemon.level.toFloat() }
    Row(
        verticalGravity = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
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
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium)
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
