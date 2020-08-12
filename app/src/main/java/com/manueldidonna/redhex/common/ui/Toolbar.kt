package com.manueldidonna.redhex.common.ui

import androidx.compose.foundation.ProvideTextStyle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val ToolbarHeight: Dp = 56.dp

@Composable
fun TranslucentToolbar(
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable RowScope.() -> Unit
) {
    ProvideTextStyle(MaterialTheme.typography.h6.copy(color = MaterialTheme.colors.onSurface)) {
        Column(
            modifier = modifier.background(color = translucentSurfaceColor()),
            verticalArrangement = Arrangement.Center,
            horizontalGravity = Alignment.CenterHorizontally
        ) {
            Row(
                verticalGravity = Alignment.CenterVertically,
                horizontalArrangement = horizontalArrangement,
                modifier = Modifier.fillMaxWidth().weight(1f),
                children = content
            )
            Divider()
        }
    }
}