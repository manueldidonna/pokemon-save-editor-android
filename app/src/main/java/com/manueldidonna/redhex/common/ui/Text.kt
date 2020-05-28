package com.manueldidonna.redhex.common.ui

import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Text
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.material.Emphasis
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.dp
import androidx.ui.unit.sp

@Composable
fun Label(modifier: Modifier = Modifier, text: String) {
    val emphasis = EmphasisAmbient.current.medium
    Box(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
        paddingBottom = 4.dp,
        paddingTop = 4.dp,
        paddingEnd = 8.dp,
        paddingStart = 8.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle2.copy(fontSize = 12.sp),
            color = emphasis.applyEmphasis(MaterialTheme.colors.onSurface)
        )
    }
}