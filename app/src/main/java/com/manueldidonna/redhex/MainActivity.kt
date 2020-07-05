package com.manueldidonna.redhex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.*
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.foundation.contentColor
import androidx.ui.foundation.isSystemInDarkTheme
import androidx.ui.graphics.toArgb
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.layout.padding
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.Category
import androidx.ui.material.icons.twotone.Inbox
import androidx.ui.material.icons.twotone.LocalLibrary
import androidx.ui.material.icons.twotone.Menu
import androidx.ui.savedinstancestate.savedInstanceState
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.ActivityResultRegistryAmbient
import com.manueldidonna.redhex.common.AssetsSpritesRetriever
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.SpritesRetrieverAmbient
import com.manueldidonna.redhex.common.ui.DarkColors
import com.manueldidonna.redhex.common.ui.LightColors
import com.manueldidonna.redhex.details.PokemonDetails
import com.manueldidonna.redhex.details.PokemonDetailsEvents
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
    data class PokemonDetails(val position: Pokemon.Position) : AppScreen()
}

class MainActivity : AppCompatActivity(), PokemonDetailsEvents {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colors = if (isSystemInDarkTheme()) DarkColors else LightColors
            ) {
                window.statusBarColor = MaterialTheme.colors.surface.toArgb()
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
            ShowScreen(screen = AppState.currentScreen, saveData = saveData)
        }
    }

    @Composable
    private fun ShowScreen(screen: AppScreen, saveData: SaveData) {
        when (screen) {
            AppScreen.Main -> MainScreen(saveData)
            is AppScreen.PokemonDetails -> {
                val (index, slot) = screen.position
                Surface {
                    PokemonDetails(
                        pokemon = saveData.getMutableStorage(index).getMutablePokemon(slot),
                        pokedex = saveData.pokedex,
                        listener = this
                    )
                }
            }
        }
    }

    private fun showPokemonDetails(position: Pokemon.Position) {
        AppState.currentScreen = AppScreen.PokemonDetails(position)
    }

    override fun goBackToPokemonList(pokemon: MutablePokemon) {
        AppState.saveData?.let { saveData ->
            val (index, slot) = pokemon.position
            saveData.getMutableStorage(index).insertPokemon(slot = slot, pokemon = pokemon)
        }
        AppState.currentScreen = AppScreen.Main
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
                BottomNavigation(backgroundColor = MaterialTheme.colors.surface) {
                    val inactiveColor = EmphasisAmbient.current.medium.applyEmphasis(contentColor())
                    for (value in BottomDestination.values()) {
                        BottomNavigationItem(
                            icon = { Icon(asset = value.icon) },
                            text = { Text(text = value.name) },
                            selected = destination == value,
                            activeColor = MaterialTheme.colors.primary,
                            inactiveColor = inactiveColor,
                            onSelected = {
                                selectedDestinationName.value = value.name
                            }
                        )
                    }
                }
            },
            bodyContent = { padding ->
                // TODO: find a better solution
                val modifier = Modifier.padding(padding)
                when (destination) {
                    BottomDestination.Storage -> PokemonList(
                        modifier,
                        saveData,
                        ::showPokemonDetails
                    )
                    BottomDestination.More -> SettingsScreen()
                    BottomDestination.Pokedex -> Pokedex(modifier, saveData.pokedex)
                    BottomDestination.Inventory -> Inventory(modifier, saveData)
                }
            }
        )
    }
}
