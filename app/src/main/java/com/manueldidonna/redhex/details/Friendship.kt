package com.manueldidonna.redhex.details

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ModifyFriendship(friendship: Int, onChange: (Int) -> Unit) {
    LabelledValue(
        label = "Friendship",
        value = friendship.toString(),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Slider(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        value = friendship.toFloat(),
        valueRange = 0f..250f,
        onValueChange = { onChange(it.roundToInt()) }
    )
}
