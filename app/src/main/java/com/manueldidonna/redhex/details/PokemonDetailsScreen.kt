package com.manueldidonna.redhex.details

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.graphics.Color
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
import com.manueldidonna.redhex.common.ui.ToolbarHeight
import com.manueldidonna.redhex.dividerColor
import com.manueldidonna.redhex.translucentSurfaceColor
import dev.chrisbanes.accompanist.coil.CoilImage
import java.io.File
import kotlin.math.roundToInt

interface PokemonDetailsEvents {
    fun goBackToPokemonList()
}

private enum class EditorTab {
    General, Moves, Stats
}

@Composable
fun PokemonDetailsScreen(
    modifier: Modifier = Modifier,
    pokemon: MutablePokemon,
    pokedex: Pokedex,
    listener: PokemonDetailsEvents
) {
    var selectedTab: EditorTab by state { EditorTab.General }
    Stack(modifier.fillMaxSize()) {
        VerticalScroller {
            Column {
                Spacer(Modifier.preferredHeight(ToolbarHeight + 48.dp))
                when (selectedTab) {
                    EditorTab.General -> {
                        SpeciesEditorField(pokemon, pokedex)
                        Divider(color = dividerColor())
                        ExperienceEditorField(pokemon)
                    }
                    EditorTab.Moves -> {
                        PokemonMovesEditor(pokemon)
                    }
                    EditorTab.Stats -> {
                    }
                }
            }
        }
        EditorToolbar(
            onNavigationClick = listener::goBackToPokemonList,
            onTabChange = { tab -> selectedTab = tab }
        )
    }
}

@Composable
private fun EditorToolbar(
    onNavigationClick: () -> Unit,
    onTabChange: (tab: EditorTab) -> Unit
) {
    val tabs = remember { EditorTab.values().toList() }
    var selectedIndex: Int by state { 0 }
    Column(
        horizontalGravity = Alignment.CenterHorizontally,
        modifier = Modifier.drawBackground(color = translucentSurfaceColor())
    ) {
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = onNavigationClick) {
                    Icon(Icons.TwoTone.ArrowBack)
                }
            },
            title = { Text(text = "Edit pokemon") },
            elevation = 0.dp,
            backgroundColor = Color.Transparent
        )
        TabRow(
            backgroundColor = translucentSurfaceColor(),
            items = tabs,
            selectedIndex = selectedIndex
        ) { index, tab ->
            Tab(
                modifier = Modifier.preferredHeight(48.dp),
                text = { Text(text = tab.name) },
                selected = index == selectedIndex,
                onSelected = {
                    onTabChange(tab)
                    selectedIndex = tabs.indexOf(tab)
                }
            )
        }
    }
}

@Composable
private fun SpeciesEditorField(pokemon: MutablePokemon, pokedex: Pokedex) {
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
                DialogItem(text = it.second) {
                    pokemon.mutator
                        .speciesId(it.first + 1)
                        .level(pokemon.level)
                        // TODO: uppercase name is a gen 1-3 detail. Abstract it
                        .nickname(it.second.toUpperCase())
                    pokedex.setEntry(Pokedex.Entry.owned(it.first + 1))
                    speciesId = pokemon.speciesId
                }
            }
        }
}


@Composable
private fun ExperienceEditorField(pokemon: MutablePokemon) {
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
