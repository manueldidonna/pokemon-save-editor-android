package com.manueldidonna.redhex.details

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.foundation.TextField
import androidx.ui.foundation.TextFieldValue
import androidx.ui.layout.*
import androidx.ui.material.Divider
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Slider
import androidx.ui.text.font.FontWeight
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.redhex.dividerColor
import kotlin.math.roundToInt

@Composable
fun PokemonStats(pokemon: MutablePokemon) {
    Column {
        Text(
            text = "Individual Values",
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 24.dp)
        )
        IndividualValuesEditorField(pokemon = pokemon)
        Divider(color = dividerColor(), modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Effort Values",
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 24.dp)
        )
        EffortValuesEditorField(pokemon = pokemon)
    }
}


@Composable
private fun IndividualValuesEditorField(pokemon: MutablePokemon) {
    var health by state(StructurallyEqual) { pokemon.iV.health }
    var attack by state(StructurallyEqual) { pokemon.iV.attack }
    var defense by state(StructurallyEqual) { pokemon.iV.defense }
    var speed by state(StructurallyEqual) { pokemon.iV.speed }
    var specialAttack by state(StructurallyEqual) { pokemon.iV.specialAttack }
    var specialDefense by state(StructurallyEqual) { pokemon.iV.specialDefense }

    val maxAllowedValue = pokemon.iV.maxAllowedValue

    fun updateIvs() {
        pokemon.iV.let {
            health = it.health
            attack = it.attack
            defense = it.defense
            speed = it.speed
            specialAttack = it.specialAttack
            specialDefense = it.specialDefense
        }
    }

    IndividualValue(
        name = "Health",
        value = health,
        maxValue = maxAllowedValue,
        onValueChange = { health = it },
        onChangeEnd = { pokemon.mutator.individualValues(health = health); updateIvs() }
    )
    IndividualValue(
        name = "Attack",
        value = attack,
        maxValue = maxAllowedValue,
        onValueChange = { attack = it },
        onChangeEnd = { pokemon.mutator.individualValues(attack = attack); updateIvs() }
    )
    IndividualValue(
        name = "Defense",
        value = defense,
        maxValue = maxAllowedValue,
        onValueChange = { defense = it },
        onChangeEnd = { pokemon.mutator.individualValues(defense = defense); updateIvs() }
    )
    IndividualValue(
        name = "Speed",
        value = speed,
        maxValue = maxAllowedValue,
        onValueChange = { speed = it },
        onChangeEnd = { pokemon.mutator.individualValues(speed = speed); updateIvs() }
    )
    IndividualValue(
        name = "Sp. Attack",
        value = specialAttack,
        maxValue = maxAllowedValue,
        onValueChange = { specialAttack = it },
        onChangeEnd = { pokemon.mutator.individualValues(specialAttack = specialAttack); updateIvs() }
    )
    IndividualValue(
        name = "Sp. Defense",
        value = specialDefense,
        maxValue = maxAllowedValue,
        onValueChange = { specialDefense = it },
        onChangeEnd = { pokemon.mutator.individualValues(specialDefense = specialDefense); updateIvs() }
    )

}

@Composable
private fun IndividualValue(
    name: String,
    value: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit,
    onChangeEnd: () -> Unit
) {
    Row(
        verticalGravity = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.subtitle1,
            // TODO: remove this fixed width when table will be available again
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value.toString(),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .width(40.dp)
                .wrapContentWidth(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium)
        )
        Slider(
            modifier = Modifier.weight(1f),
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            onValueChangeEnd = onChangeEnd,
            valueRange = 1f..maxValue.toFloat()
        )
    }
}

@Composable
private fun EffortValuesEditorField(pokemon: MutablePokemon) {
    val ev: Pokemon.EffortValues by state { pokemon.ev }

    for (i in 0 until 6) {
        val evWithName = ev[i]
        Row(
            verticalGravity = Alignment.CenterVertically,
            modifier = Modifier.height(48.dp).padding(horizontal = 24.dp)
        ) {
            Text(
                text = evWithName.first,
                style = MaterialTheme.typography.subtitle1,
                // TODO: remove this fixed width when table will be available again
                modifier = Modifier.width(100.dp)
            )
            TextField(
                modifier = Modifier.padding(horizontal = 16.dp),
                value = TextFieldValue(evWithName.second.toString()),
                onValueChange = {},
                textStyle = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

private operator fun Pokemon.EffortValues.get(index: Int): Pair<String, Int> {
    return when (index) {
        0 -> Pair("Health", health)
        1 -> Pair("Attack", attack)
        2 -> Pair("Defense", defense)
        3 -> Pair("Speed", speed)
        4 -> Pair("Sp. Attack", specialAttack)
        5 -> Pair("Sp. Defense", specialDefense)
        else -> throw IllegalStateException("Bad index: $index")
    }
}