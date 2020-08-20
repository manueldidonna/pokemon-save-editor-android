package com.manueldidonna.redhex.details

import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Pokerus
import com.manueldidonna.redhex.common.rememberMutableState
import com.manueldidonna.redhex.common.ui.ThemedDialog
import kotlin.random.Random

@Composable
fun ModifyPokerus(strain: Int, days: Int, onChange: (Pokerus) -> Unit) {
    var changePokerus by rememberMutableState { false }
    val status = when {
        strain > 0 && days == 0 -> "Cured"
        strain > 0 && days > 0 -> "Infected"
        else -> "Never infected"
    }
    LabelledValue(
        label = "Pokerus",
        value = status,
        modifier = Modifier
            .clickable(onClick = { changePokerus = true })
            .padding(horizontal = 16.dp)
    )


    if (changePokerus)
        ThemedDialog(onCloseRequest = { changePokerus = false }) {
            Column {
                Text(
                    text = "Pokerus",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(16.dp)
                )
                PokerusOption(
                    text = "Never Infected",
                    selected = strain <= 0,
                    onSelection = {
                        onChange(Pokerus.NeverInfected)
                        changePokerus = false
                    }
                )
                PokerusOption(
                    text = "Infected",
                    selected = strain > 0 && days > 0,
                    onSelection = {
                        onChange(infect())
                        changePokerus = false
                    }
                )
                PokerusOption(
                    text = "Cured",
                    selected = strain > 0 && days == 0,
                    onSelection = {
                        onChange(Pokerus(strain = Pokerus.StrainValues.random(), days = 0))
                        changePokerus = false
                    }
                )
            }
        }
}

private fun infect(): Pokerus {
    val strain = Pokerus.StrainValues.random()
    val days = Random.nextInt(1, Pokerus.maxAllowedDays(strain) + 1)
    return Pokerus(strain = strain, days = days)
}

@Composable
private fun PokerusOption(
    text: String,
    selected: Boolean,
    onSelection: () -> Unit,
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .selectable(selected = selected, onClick = onSelection)
        .padding(16.dp)
    ) {
        RadioButton(selected = selected, onClick = onSelection)
        Text(
            text = text,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
