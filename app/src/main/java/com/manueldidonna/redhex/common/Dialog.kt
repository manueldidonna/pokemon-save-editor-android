package com.manueldidonna.redhex.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AndroidDialogProperties
import androidx.compose.ui.window.Dialog

@Composable
fun SurfaceDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = AndroidDialogProperties()
    ) {
        Surface(
            modifier = Modifier.padding(vertical = 48.dp),
            shape = MaterialTheme.shapes.medium,
            content = content
        )
    }
}
