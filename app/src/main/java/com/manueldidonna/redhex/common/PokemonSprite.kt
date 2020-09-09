package com.manueldidonna.redhex.common

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun PokemonSprite(
    source: SpriteSource,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var colorFilter: ColorFilter? = null
    if (!enabled) {
        val emphasis = EmphasisAmbient.current.disabled
        colorFilter = ColorFilter.tint(emphasis.applyEmphasis(MaterialTheme.colors.onSurface))
    }
    CoilImage(
        modifier = modifier.then(SpriteSource.Size),
        colorFilter = colorFilter,
        data = source.value
    )
}

@Composable
fun ListItemWithSprite(
    modifier: Modifier = Modifier,
    primaryText: String,
    secondaryText: String?,
    spriteSource: SpriteSource,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .heightIn(minHeight = if (secondaryText.isNullOrEmpty()) 56.dp else 72.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalGravity = Alignment.CenterVertically
    ) {
        PokemonSprite(
            source = spriteSource,
            modifier = Modifier.padding(end = 16.dp),
            enabled = enabled
        )
        Column {
            Text(
                text = primaryText,
                color = primaryTextColor(enabled),
                style = MaterialTheme.typography.subtitle1
            )
            if (!secondaryText.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = secondaryText,
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.subtitle2
                )
            }
        }
    }
}

@Stable
@Composable
private fun primaryTextColor(enabled: Boolean): Color {
    val onSurfaceColor = MaterialTheme.colors.onSurface
    if (enabled) return onSurfaceColor
    return EmphasisAmbient.current.disabled.applyEmphasis(onSurfaceColor)
}