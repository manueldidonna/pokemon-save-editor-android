package com.manueldidonna.redhex.editor

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.manueldidonna.redhex.common.LabelledValue
import kotlin.math.roundToInt

@Composable
fun ModifyExperience(
    level: Int,
    experience: Int,
    onLevelChange: (Int) -> Unit,
    onExperienceChange: (Int) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        TextField(
            value = if (experience == 0) "" else experience.toString(),
            onValueChange = {
                if (it.isBlank()) {
                    onExperienceChange(0)
                } else if (it.all(Char::isDigit)) {
                    onExperienceChange(it.toInt())
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Experience points") },
            keyboardType = KeyboardType.Number
        )
        LabelledValue(label = "Level", value = level.toString())
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = level.toFloat(),
            valueRange = 1f..100f,
            onValueChange = { onLevelChange(it.roundToInt()) }
        )
    }
}