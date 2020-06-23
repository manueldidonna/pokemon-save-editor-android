package com.manueldidonna.redhex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.*
import androidx.ui.core.setContent
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.foundation.contentColor
import androidx.ui.foundation.isSystemInDarkTheme
import androidx.ui.graphics.toArgb
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.Category
import androidx.ui.material.icons.twotone.Home
import androidx.ui.material.icons.twotone.InsertChart
import androidx.ui.material.icons.twotone.Settings
import androidx.ui.savedinstancestate.savedInstanceState
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.ActivityResultRegistryAmbient
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpritesRetrieverAmbient
import com.manueldidonna.redhex.common.pokemon.PokemonSpritesRetriever
import com.manueldidonna.redhex.common.ui.DarkColors
import com.manueldidonna.redhex.common.ui.LightColors
import com.manueldidonna.redhex.details.PokemonDetailsEvents
import com.manueldidonna.redhex.details.PokemonDetailsScreen
import com.manueldidonna.redhex.inventory.Inventory
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
                    PokemonSpritesRetrieverAmbient provides PokemonSpritesRetriever.from(this),
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
                    PokemonDetailsScreen(
                        pokemon = saveData.getMutableStorage(index).getMutablePokemon(slot),
                        pokedex = saveData.getPokedex(),
                        listener = this
                    )
                }
            }
        }
    }

    private fun showPokemonDetails(position: Pokemon.Position) {
        AppState.currentScreen = AppScreen.PokemonDetails(position)
    }

    override fun goBackToPokemonList() {
        AppState.currentScreen = AppScreen.Main
    }

    private enum class BottomDestination(val icon: VectorAsset) {
        Home(Icons.TwoTone.Home),
        Pokedex(Icons.TwoTone.InsertChart),
        Inventory(Icons.TwoTone.Category),
        Settings(Icons.TwoTone.Settings)
    }

    @Composable
    private fun MainScreen(saveData: SaveData) {
        val selectedDestinationName = savedInstanceState { BottomDestination.Home.name }
        val destination = BottomDestination.valueOf(selectedDestinationName.value)
        Scaffold(
            bottomAppBar = {
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
            bodyContent = { modifier ->
                when (destination) {
                    BottomDestination.Home -> PokemonList(modifier, saveData, ::showPokemonDetails)
                    BottomDestination.Settings -> SettingsScreen()
                    BottomDestination.Pokedex -> Pokedex(modifier, saveData.getPokedex())
                    BottomDestination.Inventory -> Inventory(modifier, saveData)
                }
            }
        )
    }
}
