package com.manueldidonna.redhex

import androidx.compose.Composable
import androidx.ui.material.MaterialTheme

@Composable
fun dividerColor() = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)

@Composable
fun translucentSurfaceColor() = MaterialTheme.colors.surface.copy(alpha = 0.85f)