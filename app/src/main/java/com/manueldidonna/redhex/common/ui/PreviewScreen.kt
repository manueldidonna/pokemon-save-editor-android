package com.manueldidonna.redhex.common.ui

import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.foundation.drawBackground
import androidx.ui.material.*
import com.manueldidonna.redhex.common.ui.LightColors

@Composable
fun PreviewScreen(
    colors: ColorPalette = LightColors,
    screen: @Composable() () -> Unit
) {
    MaterialTheme(colors) {
        Surface(modifier = Modifier.drawBackground(color = MaterialTheme.colors.surface)) {
            screen()
        }
    }
}