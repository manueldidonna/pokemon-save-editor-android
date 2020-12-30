package com.manueldidonna.redhex.ui

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.twotone.AllInbox
import androidx.compose.material.icons.twotone.Category
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.redhex.DefaultIcons
import com.manueldidonna.redhex.common.registerForActivityResult
import com.manueldidonna.redhex.common.rememberMutableState
import com.manueldidonna.redhex.ui.Destination.*
import com.manueldidonna.redhex.ui.bag.BagScreen
import com.manueldidonna.redhex.ui.pokedex.PokedexScreen
import com.manueldidonna.redhex.ui.pokemon.PokemonScreen
import com.manueldidonna.redhex.ui.settings.SettingsScreen
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding

private enum class Destination(val imageVector: ImageVector) {
    Pokemon(DefaultIcons.AllInbox),
    Bag(DefaultIcons.Category),
    Settings(DefaultIcons.Settings)
}

@Composable
fun MainDestinationsScreen(saveData: SaveData) {
    var current by rememberMutableState { Pokemon }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface)
            .navigationBarsPadding(bottom = false)
            .statusBarsPadding() // TODO: better insets handling
    ) {
        Surface(modifier = Modifier.weight(1f)) {
            when (current) {
                Pokemon -> {
                    var showPokedex by savedInstanceState { false }
                    if (showPokedex) {
                        PokedexScreen(
                            pokedex = saveData.pokedex,
                            goBack = { showPokedex = false }
                        )
                    } else {
                        PokemonScreen(
                            storageSystem = saveData.storageSystem,
                            showPokedex = { showPokedex = true }
                        )
                    }
                }
                Bag -> BagScreen(bag = saveData.bag)
                Settings -> {
                    val context = AmbientContext.current
                    val exportSaveDataLauncher = registerForActivityResult(
                        contract = ActivityResultContracts.CreateDocument(),
                        onResult = { uri: Uri? ->
                            if (uri == null) return@registerForActivityResult
                            context.contentResolver.openOutputStream(uri).use { output ->
                                output?.write(saveData.exportToBytes().toByteArray())
                            }
                        }
                    )
                    SettingsScreen(
                        importSaveData = { MainState.saveData = null },
                        exportSaveData = { exportSaveDataLauncher.launch("") }
                    )
                }
            }
        }
        Surface(elevation = 8.dp, color = MaterialTheme.colors.surface) {
            BottomNavigation(
                backgroundColor = MaterialTheme.colors.surface,
                modifier = Modifier.navigationBarsPadding(),
                elevation = 0.dp
            ) {
                DestinationItem(Pokemon, current == Pokemon) { current = Pokemon }
                DestinationItem(Bag, current == Bag) { current = Bag }
                DestinationItem(Settings, current == Settings) { current = Settings }
            }
        }
    }
}

@Composable
private fun DestinationItem(
    destination: Destination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    BottomNavigationItem(
        icon = { Icon(destination.imageVector) },
        // TODO: support name translations
        label = { Text(text = destination.name) },
        selected = selected,
        onClick = onClick,
        selectedContentColor = MaterialTheme.colors.primary
    )
}