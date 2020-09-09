package com.manueldidonna.redhex.common

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun LabelledValue(modifier: Modifier = Modifier, label: String, value: String) {
    Row(
        verticalGravity = Alignment.CenterVertically,
        modifier = modifier.preferredHeight(56.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary
        )
    }
}

@Composable
fun ThemedDialog(onDismissRequest: () -> Unit, children: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier.preferredWidth(280.dp).padding(top = 48.dp, bottom = 48.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = 2.dp,
            content = children
        )
    }
}

@Composable
fun RadioButtonWithText(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .selectable(selected = selected, onClick = onClick)
        .padding(16.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = text,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

val AppBarHeight = 56.dp

@Composable
fun TranslucentSurfaceColor(): Color {
    return MaterialTheme.colors.surface.copy(alpha = 0.85f)
}

@Composable
fun TranslucentAppBar(
    modifier: Modifier = Modifier,
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val colors = MaterialTheme.colors
    TopAppBar(
        modifier = Modifier
            .height(AppBarHeight)
            .drawWithContent {
                drawContent()
                val strokeWidth = 1.dp.toPx()
                val offsetY = size.height
                drawLine(
                    color = colors.onSurface.copy(0.12f),
                    strokeWidth = strokeWidth,
                    start = Offset(x = 0f, y = offsetY),
                    end = Offset(x = size.width, y = offsetY)
                )
            }.then(modifier),
        title = { Text(text = title) },
        navigationIcon = navigationIcon,
        actions = actions,
        contentColor = colors.onSurface,
        backgroundColor = TranslucentSurfaceColor(),
        elevation = 0.dp
    )
}
