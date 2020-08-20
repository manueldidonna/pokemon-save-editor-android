package com.manueldidonna.redhex.details

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Trainer

@Composable
fun ModifyTrainer(
    name: String,
    visibleId: Int,
    secretId: Int,
    gender: Trainer.Gender,
    onChange: (Trainer) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = { onChange(Trainer(it, visibleId, secretId, gender)) },
            label = { Text(text = "Trainer Name") },
            keyboardType = KeyboardType.Text
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            TrainerIdInputField(
                modifier = Modifier.weight(1f),
                type = TrainerId.Visible,
                value = visibleId,
                onChange = { onChange(Trainer(name, it, secretId, gender)) }
            )
            Spacer(Modifier.width(8.dp))
            TrainerIdInputField(
                modifier = Modifier.weight(1f),
                type = TrainerId.Secret,
                value = secretId,
                onChange = { onChange(Trainer(name, visibleId, it, gender)) }
            )
        }
    }
}

private enum class TrainerId(val label: String) {
    Visible("Visible ID"), Secret("Secret ID")
}

@Composable
private fun TrainerIdInputField(
    modifier: Modifier,
    type: TrainerId,
    value: Int,
    onChange: (Int) -> Unit,
) {
    TextField(
        modifier = modifier,
        value = if (value <= 0) "" else value.toString(),
        onValueChange = {
            if (it.isBlank()) {
                onChange(0)
            } else if (it.all(Char::isDigit)) {
                onChange(it.toInt())
            }
        },
        label = { Text(text = type.label) },
        keyboardType = KeyboardType.Number
    )
}
