package com.manueldidonna.redhex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.contentColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Category
import androidx.compose.material.icons.twotone.Inbox
import androidx.compose.material.icons.twotone.LocalLibrary
import androidx.compose.material.icons.twotone.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.platform.setContent
import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.ActivityResultRegistryAmbient
import com.manueldidonna.redhex.common.AssetsSpritesRetriever
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.SpritesRetrieverAmbient
import com.manueldidonna.redhex.common.ui.DarkColors
import com.manueldidonna.redhex.common.ui.LightColors
import com.manueldidonna.redhex.editor.PokemonEditor
import com.manueldidonna.redhex.editor.PokemonEditorEvents
import com.manueldidonna.redhex.inventory.Inventory
import com.manueldidonna.redhex.loadsave.LoadSaveDataScreen
import com.manueldidonna.redhex.pokedex.Pokedex
import com.manueldidonna.redhex.pokemonlist.PokemonList

// TODO: find a better solution
object AppState {
    var saveData by mutableStateOf<SaveData?>(null)
    var currentScreen by mutableStateOf<AppScreen>(AppScreen.Main)
}

sealed class AppScreen {
    object Main : AppScreen()
    data class PokemonEditor(val position: Pokemon.Position) : AppScreen()
}

class MainActivity : AppCompatActivity(), PokemonEditorEvents {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colors = if (isSystemInDarkTheme()) DarkColors else LightColors
            ) {
                window.statusBarColor = MaterialTheme.colors.primarySurface.toArgb()
                Providers(
                    ActivityResultRegistryAmbient provides activityResultRegistry,
                    PokemonResourcesAmbient provides PokemonTextResources.English,
                    SpritesRetrieverAmbient provides AssetsSpritesRetriever,
                    children = { ActivityContent() }
                )
            }
        }
    }

    @Composable
    private fun ActivityContent() {
        val saveData: SaveData? = AppState.saveData
        if (saveData == null) {
            LoadSaveDataScreen()
        } else {
            ShowScreen(AppState.currentScreen, saveData)
        }
    }

    @Composable
    private fun ShowScreen(screen: AppScreen, saveData: SaveData) {
        when (screen) {
            AppScreen.Main -> MainScreen(saveData)
            is AppScreen.PokemonEditor -> {
                val pokemon = saveData[screen.position].toMutablePokemon()
                if (pokemon.isEmpty) {
                    val resources = PokemonResourcesAmbient.current.species
                    EmptyPokemonTemplate(saveData.trainer, resources).apply(pokemon)
                }
                Surface {
                    PokemonEditor(pokemon = pokemon, listener = this)
                }
            }
        }
    }

    private fun showPokemonDetails(position: Pokemon.Position) {
        AppState.currentScreen = AppScreen.PokemonEditor(position)
    }

    override fun goBackToPokemonList() {
        AppState.currentScreen = AppScreen.Main
    }

    override fun savePokemon(pokemon: Pokemon) {
        AppState.saveData?.let { saveData ->
            saveData[pokemon.position] = pokemon
            saveData.pokedex.catchPokemonById(pokemon.speciesId)
        }
        goBackToPokemonList()
    }

    private fun Pokedex.catchPokemonById(speciesId: Int) {
        setEntry(Pokedex.Entry.Immutable(speciesId, isSeen = true, isOwned = true))
    }

    private enum class BottomDestination(val icon: VectorAsset) {
        Storage(Icons.TwoTone.Inbox),
        Pokedex(Icons.TwoTone.LocalLibrary),
        Inventory(Icons.TwoTone.Category),
        More(Icons.TwoTone.Menu)
    }

    @Composable
    private fun MainScreen(saveData: SaveData) {
        val selectedDestinationName = savedInstanceState { BottomDestination.Storage.name }
        val destination = BottomDestination.valueOf(selectedDestinationName.value)
        Scaffold(
            bottomBar = {
                BottomNavigation(selected = destination) { selection ->
                    selectedDestinationName.value = selection.name
                }
            },
            bodyContent = { padding ->
                // TODO: find a better solution
                val modifier = Modifier.padding(padding)
                Crossfade(
                    current = destination,
                    animation = remember { tween(durationMillis = 225) }
                ) { destination ->
                    when (destination) {
                        BottomDestination.Storage -> {
                            PokemonList(modifier, saveData, ::showPokemonDetails)
                        }
                        BottomDestination.More -> SettingsScreen()
                        BottomDestination.Pokedex -> Pokedex(modifier, saveData.pokedex)
                        BottomDestination.Inventory -> Inventory(modifier, saveData)
                    }
                }
            }
        )
    }

    @Composable
    private fun BottomNavigation(
        selected: BottomDestination,
        onSelect: (BottomDestination) -> Unit,
    ) {
        BottomNavigation(backgroundColor = MaterialTheme.colors.surface) {
            val unselectedContentColor =
                EmphasisAmbient.current.medium.applyEmphasis(contentColor())
            for (destination in BottomDestination.values()) {
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
}
