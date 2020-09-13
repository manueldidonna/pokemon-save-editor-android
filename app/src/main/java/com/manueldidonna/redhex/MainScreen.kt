package com.manueldidonna.redhex

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.contentColor
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Category
import androidx.compose.material.icons.twotone.Inbox
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.VectorAsset
import com.manueldidonna.pk.core.*
import com.manueldidonna.redhex.bag.Bag
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.editor.PokemonEditor
import com.manueldidonna.redhex.editor.PokemonEditorEvents
import com.manueldidonna.redhex.pokedex.Pokedex
import com.manueldidonna.redhex.pokemonlist.PokemonList
import com.manueldidonna.redhex.pokemonlist.PokemonListEvents
import com.manueldidonna.redhex.settings.Settings

sealed class MainScreen {
    object Root : MainScreen()
    data class PokemonEditor(val position: Pokemon.Position) : MainScreen()
    object Pokedex : MainScreen()
}

interface MainEvents {
    fun setScreen(screen: MainScreen)
    fun removeSaveData()
}

@Composable
fun MainScreen(screen: MainScreen, saveData: SaveData, events: MainEvents) {
    when (screen) {
        MainScreen.Root -> Root(saveData, events)
        MainScreen.Pokedex ->
            Surface {
                Pokedex(saveData.pokedex) {
                    events.setScreen(MainScreen.Root)
                }
            }
        is MainScreen.PokemonEditor -> {
            Surface {
                PokemonEditor(
                    pokemon = saveData.getPokemonNotEmpty(screen.position),
                    listener = pokemonEditorEvents(saveData, events)
                )
            }
        }
    }
}

@Composable
private fun SaveData.getPokemonNotEmpty(position: Pokemon.Position): MutablePokemon {
    val storage = get(position.storageIndex)
    val pokemon = storage[position.pokemonIndex]
    if (pokemon.isEmpty()) {
        val resources = PokemonResourcesAmbient.current.species
        val pokemonFromTemplate = storage
            .pokemonFactory
            .create(EmptyPokemonTemplate(trainer, resources), position)
        require(!pokemonFromTemplate.isEmpty()) {
            "The template used to create the Pokemon isn't valid"
        }
        return pokemonFromTemplate.toMutablePokemon()
    }
    return pokemon.toMutablePokemon()
}

@Composable
private fun pokemonEditorEvents(saveData: SaveData, events: MainEvents): PokemonEditorEvents {
    return remember {
        object : PokemonEditorEvents {
            override fun applyPokemonChanges(pokemon: Pokemon) {
                saveData[pokemon.position] = pokemon
                saveData.pokedex.catchPokemonById(pokemon.speciesId)
                closeEditor()
            }

            override fun closeEditor() {
                events.setScreen(MainScreen.Root)
            }
        }
    }
}

@Composable
private fun Root(saveData: SaveData, events: MainEvents) {
    val selectedDestinationName = savedInstanceState { RootDestination.Storage.name }
    val selectedEditor = RootDestination.valueOf(selectedDestinationName.value)
    Scaffold(
        bottomBar = {
            RootBottomNavigation(selected = selectedEditor) { editor ->
                selectedDestinationName.value = editor.name
            }
        },
        bodyContent = { padding ->
            Crossfade(
                current = selectedEditor,
                animation = tween(durationMillis = 225),
                modifier = Modifier.padding(padding)
            ) { editor ->
                when (editor) {
                    RootDestination.Storage -> PokemonList(saveData, pokemonListEvents(events))
                    RootDestination.Settings -> Settings(saveData, events::removeSaveData)
                    RootDestination.Bag -> Bag(saveData.bag)
                }
            }
        }
    )
}

@Composable
private fun pokemonListEvents(events: MainEvents): PokemonListEvents {
    return remember {
        object : PokemonListEvents {
            override fun editPokemonByPosition(position: Pokemon.Position) {
                events.setScreen(MainScreen.PokemonEditor(position))
            }

            override fun showPokedex() {
                events.setScreen(MainScreen.Pokedex)
            }
        }
    }
}

private enum class RootDestination(val icon: VectorAsset) {
    Storage(Icons.TwoTone.Inbox),
    Bag(Icons.TwoTone.Category),
    Settings(Icons.TwoTone.Settings)
}

@Composable
private fun RootBottomNavigation(
    selected: RootDestination,
    onSelect: (RootDestination) -> Unit,
) {
    val destinations = remember { RootDestination.values() }
    val unselectedContentColor = EmphasisAmbient.current.medium.applyEmphasis(contentColor())
    BottomNavigation(backgroundColor = MaterialTheme.colors.surface) {
        for (destination in destinations) {
            BottomNavigationItem(
                icon = { Icon(asset = destination.icon) },
                label = { Text(text = destination.name) },
                selected = selected == destination,
                selectedContentColor = MaterialTheme.colors.primary,
                unselectedContentColor = unselectedContentColor,
                onSelect = { onSelect(destination) }
            )
        }
    }
}
