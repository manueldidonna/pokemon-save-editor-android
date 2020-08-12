package com.manueldidonna.redhex.common.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun translucentSurfaceColor() = MaterialTheme.colors.surface.copy(alpha = 0.85f)

val LightColors = lightColors(
    primary = Color(0xFFf54149),
    primaryVariant = Color(0xFFff7775),
    secondary = Color(0xFF00c5ad),
    secondaryVariant = Color(0xFF5ff9e0)
)

val DarkColors = darkColors(
    primary = Color(0xFFea696e),
    primaryVariant = Color(0xFFf39497),
    secondary = Color(0xFF5ff9e0)
)
