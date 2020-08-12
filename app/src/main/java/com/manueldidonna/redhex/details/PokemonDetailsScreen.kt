package com.manueldidonna.redhex.details

import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.redhex.common.ui.ToolbarHeight
import com.manueldidonna.redhex.common.ui.translucentSurfaceColor

interface PokemonDetailsEvents {
    fun goBackToPokemonList(pokemon: MutablePokemon)
}

private enum class EditorTab {
    General, Moves, Stats
}

@Composable
fun PokemonDetails(
    modifier: Modifier = Modifier,
    pokemon: MutablePokemon,
    listener: PokemonDetailsEvents
) {
    val selectedTab = remember { mutableStateOf(EditorTab.General) }
    Stack(modifier.fillMaxSize()) {
        ScrollableColumn {
            Spacer(Modifier.preferredHeight(ToolbarHeight + 48.dp))
            when (selectedTab.value) {
                EditorTab.General -> PokemonGeneral(pokemon)
                EditorTab.Moves -> PokemonMoves(pokemon)
                EditorTab.Stats -> TODO()
            }
        }
        EditorToolbar(
            onNavigationClick = { listener.goBackToPokemonList(pokemon) },
            onTabChange = { tab -> selectedTab.value = tab }
        )
    }
}

@Composable
private fun EditorToolbar(
    onNavigationClick: () -> Unit,
    onTabChange: (tab: EditorTab) -> Unit
) {
    val tabs = remember { EditorTab.values().toList() }
    val selectedIndex = remember { mutableStateOf(0) }
    Column(
        horizontalGravity = Alignment.CenterHorizontally,
        modifier = Modifier.background(color = translucentSurfaceColor())
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
            selectedIndex = selectedIndex.value
        ) { index, tab ->
            Tab(
                modifier = Modifier.preferredHeight(48.dp),
                text = { Text(text = tab.name) },
                selected = index == selectedIndex.value,
                onSelected = {
                    onTabChange(tab)
                    selectedIndex.value = tabs.indexOf(tab)
                }
            )
        }
    }
}
