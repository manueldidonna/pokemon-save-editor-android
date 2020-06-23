package com.manueldidonna.redhex.common.ui

import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.foundation.Dialog
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.padding
import androidx.ui.layout.preferredWidth
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.unit.dp

@Composable
fun ThemedDialog(onCloseRequest: () -> Unit, children: @Composable() () -> Unit) {
    val currentColors = MaterialTheme.colors
    val currentTypography = MaterialTheme.typography
    Dialog(onCloseRequest = onCloseRequest) {
        MaterialTheme(colors = currentColors, typography = currentTypography) {
            Surface(
                modifier = Modifier.preferredWidth(280.dp).padding(top = 48.dp, bottom = 48.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = 2.dp,
                content = children
            )
        }
    }
}
