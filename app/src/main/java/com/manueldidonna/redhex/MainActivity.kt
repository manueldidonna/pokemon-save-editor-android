package com.manueldidonna.redhex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.Providers
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.foundation.isSystemInDarkTheme
import androidx.ui.graphics.toArgb
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.layout.Stack
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.padding
import androidx.ui.material.BottomNavigation
import androidx.ui.material.BottomNavigationItem
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.Home
import androidx.ui.material.icons.twotone.List
import androidx.ui.material.icons.twotone.Settings
import androidx.ui.savedinstancestate.savedInstanceState
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.resources.PokemonResources
import com.manueldidonna.redhex.common.ActivityResultRegistryAmbient
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpritesRetrieverAmbient
import com.manueldidonna.redhex.common.pokemon.PokemonSpritesRetriever
import com.manueldidonna.redhex.common.ui.DarkColors
import com.manueldidonna.redhex.common.ui.LightColors
import com.manueldidonna.redhex.details.PokemonDetailsEvents
import com.manueldidonna.redhex.details.PokemonDetailsScreen
import com.manueldidonna.redhex.home.HomeEvents
import com.manueldidonna.redhex.home.HomeScreen
import com.manueldidonna.redhex.pokedex.PokedexScreen

@Model
object MainState {
    var saveData: SaveData? = null
}

class MainActivity : AppCompatActivity(), HomeEvents, PokemonDetailsEvents {

    @Model
    private class RouteState {
        var route: Route = Route.Root

        sealed class Route {
            object Root : Route()
            data class Details(val position: Pokemon.Position) : Route()
        }
    }

    private val routeState = RouteState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(if (isSystemInDarkTheme()) DarkColors else LightColors) {
                Providers(
                    ActivityResultRegistryAmbient provides activityResultRegistry,
                    PokemonResourcesAmbient provides PokemonResources.English,
                    PokemonSpritesRetrieverAmbient provides PokemonSpritesRetriever.from(this)
                ) {
                    window.statusBarColor = MaterialTheme.colors.surface.toArgb()
                    ActivityScreen()
                }
            }
        }
    }

    @Composable
    private fun ActivityScreen() {
        val saveData: SaveData? = MainState.saveData
        if (saveData == null) {
            LoadSaveDataScreen()
        } else {
            val route = routeState.route
            if (route is RouteState.Route.Details) {
                val (index, slot) = route.position
                Surface {
                    PokemonDetailsScreen(
                        pokemon = saveData.getMutableStorage(index).getMutablePokemon(slot),
                        pokedex = saveData.getPokedex(),
                        listener = this
                    )
                }
            } else {
                SurfaceWithBottomNavigation { destination, modifier ->
                    when (destination) {
                        BottomDestination.Home -> HomeScreen(modifier, saveData, this)
                        BottomDestination.Settings -> SettingsScreen()
                        BottomDestination.Pokedex -> PokedexScreen(modifier, saveData.getPokedex())
                    }
                }
            }
        }
    }

    override fun showPokemonDetails(position: Pokemon.Position) {
        routeState.route = RouteState.Route.Details(position)
    }

    override fun goBackToPokemonList() {
        routeState.route = RouteState.Route.Root
    }

    private enum class BottomDestination(val icon: VectorAsset) {
        Home(Icons.TwoTone.Home),
        Pokedex(Icons.TwoTone.List),
        Settings(Icons.TwoTone.Settings)
    }

    @Composable
    private fun SurfaceWithBottomNavigation(
        content: @Composable() (BottomDestination, Modifier) -> Unit
    ) {
        val selectedDestinationName = savedInstanceState { BottomDestination.Home.name }
        val destination = BottomDestination.valueOf(selectedDestinationName.value)


        Stack(modifier = Modifier.fillMaxSize()) {
            Surface(modifier = Modifier.matchParentSize()) {
                content(destination, Modifier.padding(bottom = 56.dp))
            }
            BottomNavigation(
                backgroundColor = MaterialTheme.colors.surface,
                modifier = Modifier.gravity(Alignment.BottomStart)
            ) {
                for (value in BottomDestination.values()) {
                    BottomNavigationItem(
                        icon = { Icon(asset = value.icon) },
                        text = { Text(text = value.name) },
                        selected = destination == value,
                        onSelected = {
                            selectedDestinationName.value = value.name
                        }
                    )
                }
            }
        }
    }
}
