package com.manueldidonna.redhex.common.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

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
