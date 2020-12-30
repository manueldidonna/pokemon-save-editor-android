package com.manueldidonna.redhex

import androidx.activity.ComponentActivity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientContext
import androidx.compose.ui.platform.AmbientView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

val DefaultIcons = Icons.TwoTone

@Composable
fun PokemonEditorTheme(content: @Composable () -> Unit) {
    // TODO: support dark theme
    MaterialTheme(
        shapes = Shapes(
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(0.dp)
        ),
        colors = LightColors,
        content = content
    )
}

@Composable
fun EdgeToEdgeContent(content: @Composable () -> Unit) {
    val view = AmbientView.current
    val window = (AmbientContext.current as ComponentActivity).window
    val isLightTheme = MaterialTheme.colors.isLight
    DisposableEffect(view, window, isLightTheme) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, view)?.run {
            isAppearanceLightStatusBars = isLightTheme
            isAppearanceLightNavigationBars = isLightTheme
        }
        onDispose {}
    }
    ProvideWindowInsets(content = content)
}

private val LightColors = lightColors(
    primary = Color(0xFF416480),
    primaryVariant = Color(0XFF113a53),
    onPrimary = Color.White,
    secondary = Color(0XFFf15c5e),
    secondaryVariant = Color(0XFFb92634),
    onSecondary = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    background = Color.White,
    onBackground = Color.Black
)