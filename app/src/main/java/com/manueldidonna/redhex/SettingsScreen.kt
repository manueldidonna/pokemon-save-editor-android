package com.manueldidonna.redhex

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.padding
import androidx.ui.material.OutlinedButton
import androidx.ui.unit.dp

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalGravity = Alignment.CenterHorizontally
    ) {
        val padding = Modifier.padding(8.dp)
        OutlinedButton(modifier = padding, onClick = { MainState.saveData = null }) {
            Text(text = "CHOOSE ANOTHER SAVEDATA")
        }
        OutlinedButton(modifier = padding, onClick = { /** TODO: export save data **/ }) {
            Text(text = "EXPORT SAVEDATA")
        }
    }
}