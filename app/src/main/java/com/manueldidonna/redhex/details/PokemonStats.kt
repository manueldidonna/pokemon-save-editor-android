package com.manueldidonna.redhex.details

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.foundation.TextFieldValue
import androidx.ui.input.KeyboardType
import androidx.ui.layout.*
import androidx.ui.material.Divider
import androidx.ui.material.FilledTextField
import androidx.ui.material.MaterialTheme
import androidx.ui.text.TextRange
import androidx.ui.text.font.FontWeight
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokemon

private class ObservableStatisticValues(
    values: Pokemon.StatisticValues
) : Pokemon.StatisticValues {
    override var health by mutableStateOf(values.health, StructurallyEqual)
    override var attack by mutableStateOf(values.attack, StructurallyEqual)
    override var defense by mutableStateOf(values.defense, StructurallyEqual)
    override var specialAttack by mutableStateOf(values.specialAttack, StructurallyEqual)
    override var specialDefense by mutableStateOf(values.specialDefense, StructurallyEqual)
    override var speed by mutableStateOf(values.speed, StructurallyEqual)

    fun update(values: Pokemon.StatisticValues) {
        health = values.health
        attack = values.attack
        defense = values.defense
        specialAttack = values.specialAttack
        specialDefense = values.specialDefense
        speed = values.speed
    }
}

@Composable
fun PokemonStats(pokemon: MutablePokemon) {
    val ivs = remember { ObservableStatisticValues(pokemon.iV) }
    val evs = remember { ObservableStatisticValues(pokemon.eV) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        StatValue(
            statName = "Health",
            effortValue = evs.health,
            onEffortValueChange = {
                pokemon.mutator.effortValues(health = it); evs.update(pokemon.eV)
            },
            individualValue = ivs.health,
            onIndividualValueChange = {
                pokemon.mutator.individualValues(health = it); ivs.update(pokemon.iV)
            }
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        StatValue(
            statName = "Attack",
            effortValue = evs.attack,
            onEffortValueChange = {
                pokemon.mutator.effortValues(attack = it); evs.update(pokemon.eV)
            },
            individualValue = ivs.attack,
            onIndividualValueChange = {
                pokemon.mutator.individualValues(attack = it); ivs.update(pokemon.iV)
            }
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        StatValue(
            statName = "Defense",
            effortValue = evs.defense,
            onEffortValueChange = {
                pokemon.mutator.effortValues(defense = it); evs.update(pokemon.eV)
            },
            individualValue = ivs.defense,
            onIndividualValueChange = {
                pokemon.mutator.individualValues(defense = it); ivs.update(pokemon.iV)
            }
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        StatValue(
            statName = "Speed",
            effortValue = evs.speed,
            onEffortValueChange = {
                pokemon.mutator.effortValues(speed = it); evs.update(pokemon.eV)
            },
            individualValue = ivs.speed,
            onIndividualValueChange = {
                pokemon.mutator.individualValues(speed = it); ivs.update(pokemon.iV)
            }
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        StatValue(
            statName = "Special Attack",
            effortValue = evs.specialAttack,
            onEffortValueChange = {
                pokemon.mutator.effortValues(specialAttack = it); evs.update(pokemon.eV)
            },
            individualValue = ivs.specialAttack,
            onIndividualValueChange = {
                pokemon.mutator.individualValues(specialAttack = it); ivs.update(pokemon.iV)
            }
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        StatValue(
            statName = "Special Defense",
            effortValue = evs.specialDefense,
            onEffortValueChange = {
                pokemon.mutator.effortValues(specialDefense = it); evs.update(pokemon.eV)
            },
            individualValue = ivs.specialDefense,
            onIndividualValueChange = {
                pokemon.mutator.individualValues(specialDefense = it); ivs.update(pokemon.iV)
            }
        )
    }
}

@Composable
private fun StatValue(
    statName: String,
    effortValue: Int,
    onEffortValueChange: (Int) -> Unit,
    individualValue: Int,
    onIndividualValueChange: (Int) -> Unit
) {
    var effortTextSelection by state { TextRange(0, 0) }
    var individualTextSelection by state { TextRange(0, 0) }

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(
            text = statName,
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalGravity = Alignment.CenterVertically) {
            FilledTextField(
                modifier = Modifier.weight(4f),
                value = TextFieldValue(
                    text = if (effortValue == 0) "" else effortValue.toString(),
                    selection = effortTextSelection
                ),
                onValueChange = {
                    if (it.text.all(Char::isDigit)) {
                        effortTextSelection = it.selection
                        onEffortValueChange(it.text.ifEmpty { "0" }.toInt())
                    }
                },
                label = { Text(text = "Effort Value") },
                keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.width(16.dp))
            FilledTextField(
                modifier = Modifier.weight(3f),
                value = TextFieldValue(
                    text = if (individualValue == 0) "" else individualValue.toString(),
                    selection = individualTextSelection
                ),
                onValueChange = {
                    if (it.text.all(Char::isDigit)) {
                        individualTextSelection = it.selection
                        onIndividualValueChange(it.text.ifEmpty { "0" }.toInt())
                    }
                },
                label = { Text(text = "Individual Value") },
                keyboardType = KeyboardType.Number
            )
        }
    }
}
