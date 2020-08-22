package com.manueldidonna.redhex.editor

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
