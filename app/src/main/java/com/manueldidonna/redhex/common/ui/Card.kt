package com.manueldidonna.redhex.common.ui

import androidx.compose.Composable
import androidx.compose.Stable
import androidx.ui.core.Modifier
import androidx.ui.foundation.Border
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.material.Card
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.dp

@Composable
val CardBorder: Border
    get() = Border(size = 1.dp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f))

@Composable
fun OutlinedCard(modifier: Modifier = Modifier, content: @Composable() () -> Unit) {
    Card(
        modifier = modifier,
        color = MaterialTheme.colors.surface,
        shape = RoundedCornerShape(8.dp),
        border = CardBorder,
        elevation = 0.dp,
        content = content
    )
}
