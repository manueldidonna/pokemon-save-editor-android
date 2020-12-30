package com.manueldidonna.redhex.common

import androidx.compose.material.AmbientContentColor
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

val AppBarHeight = 56.dp

val AppBarElevation = 4.dp

@Composable
fun appBarIconTint(): Color = AmbientContentColor.current.copy(alpha = ContentAlpha.medium)

@Composable
fun AppBarIconButton(
    icon: ImageVector,
    tint: Color = appBarIconTint(),
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(icon, tint = tint)
    }
}