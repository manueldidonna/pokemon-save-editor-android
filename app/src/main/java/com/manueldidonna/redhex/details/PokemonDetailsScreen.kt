package com.manueldidonna.redhex.details

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.drawBackground
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.IconButton
import androidx.ui.material.Tab
import androidx.ui.material.TabRow
import androidx.ui.material.TopAppBar
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.ArrowBack
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.redhex.common.ui.ToolbarHeight
import com.manueldidonna.redhex.translucentSurfaceColor

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
                    EditorTab.General -> PokemonGeneral(pokemon, pokedex)
                    EditorTab.Moves -> PokemonMoves(pokemon)
                    EditorTab.Stats -> PokemonStats(pokemon)
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
            backgroundColor = Color.Transparent,
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


