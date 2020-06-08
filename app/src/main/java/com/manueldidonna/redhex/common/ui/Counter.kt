package com.manueldidonna.redhex.common.ui

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.ContentGravity
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.graphics.vector.VectorAsset
import androidx.ui.layout.Row
import androidx.ui.layout.preferredSize
import androidx.ui.layout.wrapContentWidth
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.Add
import androidx.ui.material.icons.twotone.Remove
import androidx.ui.unit.dp

@Composable
fun Counter(
    modifier: Modifier,
    value: Int,
    enableIncrease: Boolean = true,
    enableDecrease: Boolean = true,
    onValueChanged: (Int) -> Unit
) {
    Row(modifier = modifier, verticalGravity = Alignment.CenterVertically) {
        CounterButton(
            icon = Icons.TwoTone.Remove,
            enable = enableDecrease,
            onClick = { onValueChanged(value - 1) })
        Text(
            text = value.toString(),
            modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.body2,
            color = EmphasisAmbient.current.high.applyEmphasis(MaterialTheme.colors.onSurface)
        )
        CounterButton(
            icon = Icons.TwoTone.Add,
            enable = enableIncrease,
            onClick = { onValueChanged(value + 1) })
    }
}

@Composable
private fun CounterButton(icon: VectorAsset, enable: Boolean, onClick: () -> Unit) {
    val emphasis = EmphasisAmbient.current.run { if (enable) high else disabled }
    if (enable) {
        IconButton(onClick = onClick) {
            Icon(asset = icon, tint = emphasis.applyEmphasis(MaterialTheme.colors.onSurface))
        }
    } else {
        Box(modifier = Modifier.preferredSize(48.dp), gravity = ContentGravity.Center) {
            Icon(asset = icon, tint = emphasis.applyEmphasis(MaterialTheme.colors.onSurface))
        }
    }
}
