package com.manueldidonna.redhex.common.ui

import androidx.compose.foundation.background
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PreviewScreen(
    colors: Colors = LightColors,
    screen: @Composable () -> Unit
) {
    MaterialTheme(colors) {
        Surface(modifier = Modifier.background(color = MaterialTheme.colors.surface)) {
            screen()
        }
    }
}