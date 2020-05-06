package com.manueldidonna.redhex.common

import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.foundation.drawBackground
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.material.darkColorPalette
import androidx.ui.material.lightColorPalette

@Composable
fun PreviewScreen(
    isLightTheme: Boolean = true,
    screen: @Composable() () -> Unit
) {
    MaterialTheme(if (isLightTheme) lightColorPalette() else darkColorPalette()) {
        Surface(modifier = Modifier.drawBackground(color = MaterialTheme.colors.surface)) {
            screen()
        }
    }
}