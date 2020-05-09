package com.manueldidonna.redhex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.Providers
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.*
import androidx.ui.graphics.toArgb
import androidx.ui.layout.padding
import androidx.ui.material.BottomNavigation
import androidx.ui.material.BottomNavigationItem
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.Home
import androidx.ui.material.icons.twotone.Settings
import androidx.ui.savedinstancestate.savedInstanceState
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.resources.PokemonResources
import com.manueldidonna.redhex.common.ActivityResultRegistryAmbient
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpritesRetrieverAmbient
import com.manueldidonna.redhex.common.pokemon.PokemonSpritesRetriever
import com.manueldidonna.redhex.common.ui.DarkColors
import com.manueldidonna.redhex.common.ui.LightColors
import com.manueldidonna.redhex.home.HomeScreen


@Model
object MainState {
    var saveData: SaveData? = null
}

class MainActivity : AppCompatActivity() {
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
            SurfaceWithBottomNavigation { destination, modifier ->
                when (destination) {
                    BottomDestination.Home -> HomeScreen(modifier, saveData)
                    BottomDestination.Settings -> SettingsScreen()
                }
            }
        }
    }

    private enum class BottomDestination {
        Home, Settings
    }

    @Composable
    private fun SurfaceWithBottomNavigation(
        content: @Composable() (BottomDestination, Modifier) -> Unit
    ) {
        val selectedDestinationName = savedInstanceState { BottomDestination.Home.name }
        val destination = BottomDestination.valueOf(selectedDestinationName.value)

        Box(gravity = ContentGravity.BottomCenter) {
            Surface {
                content(destination, Modifier.padding(bottom = 56.dp))
            }
            BottomNavigation(backgroundColor = MaterialTheme.colors.surface) {
                BottomNavigationItem(
                    icon = { Icon(asset = Icons.TwoTone.Home) },
                    text = { Text(text = "Home") },
                    selected = destination == BottomDestination.Home,
                    onSelected = {
                        selectedDestinationName.value = BottomDestination.Home.name
                    }
                )
                BottomNavigationItem(
                    icon = { Icon(asset = Icons.TwoTone.Settings) },
                    text = { Text(text = "Settings") },
                    selected = destination == BottomDestination.Settings,
                    onSelected = {
                        selectedDestinationName.value = BottomDestination.Settings.name
                    }
                )
            }
        }
    }
}
