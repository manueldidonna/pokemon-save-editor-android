package com.manueldidonna.redhex

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.setContent
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.*
import com.manueldidonna.redhex.settings.ImportSaveDataFromFile

class MainActivity : AppCompatActivity(), MainEvents {

    // TODO: find a better solution
    private object MainState {
        var saveData by mutableStateOf<SaveData?>(null)
        var currentScreen by mutableStateOf<MainScreen>(MainScreen.Root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colors = if (isSystemInDarkTheme()) DarkColors else LightColors) {
                window.statusBarColor = MaterialTheme.colors.surface.toArgb()
                Providers(
                    ActivityResultRegistryAmbient provides activityResultRegistry,
                    PokemonResourcesAmbient provides PokemonTextResources.English,
                    SpritesRetrieverAmbient provides AssetsSpritesRetriever
                ) {
                    val saveData: SaveData? = MainState.saveData
                    if (saveData == null) {
                        ImportSaveDataFromFile {
                            MainState.saveData = it
                        }
                    } else {
                        MainScreen(
                            screen = MainState.currentScreen,
                            saveData = saveData,
                            events = this
                        )
                    }
                }
            }
        }
    }

    override fun setScreen(screen: MainScreen) {
        MainState.currentScreen = screen
    }

    override fun removeSaveData() {
        MainState.saveData = null
    }
}
