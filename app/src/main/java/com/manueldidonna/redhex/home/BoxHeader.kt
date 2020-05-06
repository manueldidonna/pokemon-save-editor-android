package com.manueldidonna.redhex.home

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.layout.*
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.ArrowBack
import androidx.ui.material.icons.twotone.ArrowForward
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import com.manueldidonna.redhex.common.PreviewScreen

@Composable
fun BoxHeader(
    modifier: Modifier = Modifier,
    boxName: String,
    onBack: () -> Unit,
    onForward: () -> Unit
) {
    Row(modifier = modifier.fillMaxWidth(), verticalGravity = Alignment.CenterVertically) {
        Text(
            text = boxName,
            style = MaterialTheme.typography.h6.copy(fontSize = 22.sp),
            modifier = Modifier.weight(1f).padding(start = 24.dp, end = 16.dp)
        )
        IconButton(onClick = onBack) {
            Icon(Icons.TwoTone.ArrowBack, tint = MaterialTheme.colors.primary)
        }
        IconButton(onClick = onForward) {
            Icon(Icons.TwoTone.ArrowForward, tint = MaterialTheme.colors.primary)
        }
        Spacer(modifier = modifier.preferredWidth(16.dp))
    }
}

@Preview
@Composable
private fun PreviewBoxHeader() {
    PreviewScreen {
        BoxHeader(boxName = "Box 1", onBack = {}, onForward = {})
    }
}
