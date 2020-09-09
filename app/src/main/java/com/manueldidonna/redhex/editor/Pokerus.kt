package com.manueldidonna.redhex.editor

import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Pokerus
import com.manueldidonna.redhex.common.LabelledValue
import com.manueldidonna.redhex.common.RadioButtonWithText
import com.manueldidonna.redhex.common.ThemedDialog
import com.manueldidonna.redhex.common.rememberMutableState
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
        ThemedDialog(onDismissRequest = { changePokerus = false }) {
            Column {
                Text(
                    text = "Pokerus",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(16.dp)
                )
                RadioButtonWithText(
                    text = "Never Infected",
                    selected = strain <= 0,
                    onClick = {
                        onChange(Pokerus.NeverInfected)
                        changePokerus = false
                    }
                )
                RadioButtonWithText(
                    text = "Infected",
                    selected = strain > 0 && days > 0,
                    onClick = {
                        onChange(infect())
                        changePokerus = false
                    }
                )
                RadioButtonWithText(
                    text = "Cured",
                    selected = strain > 0 && days == 0,
                    onClick = {
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
