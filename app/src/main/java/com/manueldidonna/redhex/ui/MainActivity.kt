package com.manueldidonna.redhex.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.setContent
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.redhex.EdgeToEdgeContent
import com.manueldidonna.redhex.PokemonEditorTheme
import com.manueldidonna.redhex.ProvidePokemonResources
import com.manueldidonna.redhex.ui.importsavedata.ImportSaveDataScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PokemonEditorTheme {
                EdgeToEdgeContent {
                    ProvidePokemonResources {
                        val saveData: SaveData? = MainState.saveData
                        if (saveData == null) {
                            ImportSaveDataScreen { MainState.saveData = it }
                        } else {
                            MainDestinationsScreen(saveData)
                        }
                    }
                }
            }
        }
    }
}
