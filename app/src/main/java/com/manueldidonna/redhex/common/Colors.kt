package com.manueldidonna.redhex.common

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun translucentSurfaceColor() = MaterialTheme.colors.surface.copy(alpha = 0.85f)

val LightColors = lightColors(
    primary = Color(0xFF3d946a),
    primaryVariant = Color(0xFF6fc598),
    onPrimary = Color.White,
    secondary = Color(0xFFe94e49),
    secondaryVariant = Color(0xFFff8175),
    onSecondary = Color.Black,
    surface = Color(0xFFf5f5f5),
    onSurface = Color.Black,
    background = Color(0xFFf5f5f5),
    onBackground = Color.Black
)

val DarkColors = darkColors(
    primary = Color(0xFF81cca8),
    primaryVariant = Color(0xFFb3ffda),
    onPrimary = Color.Black,
    secondary = Color(0xFFe06f6d),
    onSecondary = Color.Black,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White
)