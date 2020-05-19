package com.manueldidonna.redhex.common.ui

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.ProvideTextStyle
import androidx.ui.foundation.drawBackground
import androidx.ui.layout.*
import androidx.ui.material.Divider
import androidx.ui.material.MaterialTheme
import com.manueldidonna.redhex.dividerColor
import com.manueldidonna.redhex.translucentSurfaceColor

@Composable
fun TranslucentToolbar(
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable RowScope.() -> Unit
) {
    ProvideTextStyle(
        MaterialTheme.typography.h6.copy(color = MaterialTheme.colors.onSurface)
    ) {
        Column(
            modifier = modifier.drawBackground(color = translucentSurfaceColor()),
            verticalArrangement = Arrangement.Center,
            horizontalGravity = Alignment.CenterHorizontally
        ) {
            Row(
                verticalGravity = Alignment.CenterVertically,
                horizontalArrangement = horizontalArrangement,
                modifier = Modifier.fillMaxWidth().weight(1f),
                children = content
            )
            Divider(color = dividerColor())
        }
    }
}