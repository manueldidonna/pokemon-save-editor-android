package com.manueldidonna.redhex

import android.os.Bundle
import androidx.activity.result.ActivityResultRegistry
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.Model
import androidx.compose.Providers
import androidx.compose.staticAmbientOf
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.layout.Column
import androidx.ui.layout.fillMaxSize
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.Home
import androidx.ui.material.icons.twotone.Settings
import androidx.ui.savedinstancestate.savedInstanceState
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.redhex.home.HomeScreen

val ActivityResultRegistryAmbient = staticAmbientOf<ActivityResultRegistry>()

@Model
object MainState {
    var saveData: SaveData? = null
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(lightColorPalette()) {
                Providers(
                    ActivityResultRegistryAmbient provides activityResultRegistry
                ) {
                    HandleScreens()
                }
            }
        }
    }

    @Composable
    private fun HandleScreens() {
        val saveData: SaveData? = MainState.saveData
        if (saveData == null)
            LoadSaveDataScreen()
        else {
            WrapWithBottomNavigation { destination ->
                when (destination) {
                    BottomDestination.Home -> HomeScreen(saveData)
                    BottomDestination.Settings -> SettingsScreen()
                }
            }
        }
    }

    private enum class BottomDestination {
        Home, Settings
    }

    @Composable
    private fun WrapWithBottomNavigation(content: @Composable() (BottomDestination) -> Unit) {
        val selectedDestinationName = savedInstanceState { BottomDestination.Home.name }
        val destination = BottomDestination.valueOf(selectedDestinationName.value)

        Column(modifier = Modifier.fillMaxSize()) {
            Surface(modifier = Modifier.weight(1f)) {
                content(destination)
            }
            BottomNavigation {
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
