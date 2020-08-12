package com.manueldidonna.redhex.details

import androidx.compose.foundation.Box
import androidx.compose.foundation.ContentGravity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Trainer
import com.manueldidonna.pk.core.Version
import com.manueldidonna.redhex.common.*
import com.manueldidonna.redhex.common.ui.ThemedDialog
import kotlin.math.roundToInt

@Composable
fun PokemonGeneral(pokemon: MutablePokemon) {
    val resources = PokemonResourcesAmbient.current
    val speciesId = remember { mutableStateOf(pokemon.speciesId) }
    val speciesName: String = remember(speciesId.value) {
        resources.species.getSpeciesById(speciesId.value)
    }
    val isShiny = remember { mutableStateOf(pokemon.isShiny) }

    Species(pokemon.version, speciesId.value, speciesName, isShiny.value) {
        pokemon.mutator
            .speciesId(it)
            .level(pokemon.level)
            .nickname(resources.species.getSpeciesById(it), ignoreCase = true)
        speciesId.value = pokemon.speciesId
    }
    Divider()
    Shiny(
        isShiny = isShiny.value,
        onChange = {
            pokemon.mutator.shiny(it)
            isShiny.value = pokemon.isShiny
        }
    )
    Divider()
    Experience(pokemon)
    Divider()
    val trainer = state { pokemon.trainer }
    Trainer(trainer = trainer.value) {
        pokemon.mutator.trainer(it, ignoreNameCase = false)
        trainer.value = pokemon.trainer
    }
}

@Composable
private fun Species(
    version: Version,
    speciesId: Int,
    speciesName: String,
    isShiny: Boolean,
    onSpeciesChange: (Int) -> Unit
) {
    val resources = PokemonResourcesAmbient.current.species
    val spritesRetriever = SpritesRetrieverAmbient.current

    val spriteSource: SpriteSource = remember(speciesId) {
        spritesRetriever.getPokemonSprite(speciesId, shiny = isShiny)
    }

    var showSpeciesDialog by rememberMutableState { false }

    ListItem(
        icon = {
            Box(gravity = ContentGravity.Center, modifier = Modifier.size(40.dp)) {
                PokemonSprite(source = spriteSource)
            }
        },
        text = { Text(text = speciesName) },
        onClick = { showSpeciesDialog = true }
    )

    if (showSpeciesDialog) {
        val onCloseRequest = { showSpeciesDialog = false }
        ThemedDialog(onCloseRequest = onCloseRequest) {
            // TODO: manage empty species id
            val pokemonNames = remember { resources.getAllSpecies(version).drop(1) }
            LazyColumnForIndexed(items = pokemonNames) { i, name ->
                ListItem(
                    text = name,
                    onClick = {
                        onSpeciesChange(i + 1)
                        onCloseRequest()
                    }
                )
            }
        }
    }
}

@Composable
private fun Shiny(isShiny: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        text = { Text(text = "Shiny") },
        trailing = { Checkbox(checked = isShiny, onCheckedChange = onChange) },
        onClick = { onChange(!isShiny) }
    )
}

@Composable
private fun Experience(pokemon: MutablePokemon) {
    var level by rememberMutableState { pokemon.level }
    Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp)) {
        NumberField(
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            label = "Experience points",
            value = 24594,
            onValueChange = {})
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalGravity = Alignment.CenterVertically) {
            Text(
                text = "Level",
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = level.toString(),
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = level.toFloat(),
            valueRange = 1f..100f,
            onValueChange = { level = it.roundToInt() },
            onValueChangeEnd = { pokemon.mutator.level(level) }
        )
    }
}

@Composable
private fun Trainer(trainer: Trainer, onTrainerChange: (Trainer) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            Modifier.fillMaxWidth(),
            label = "Trainer Name",
            value = trainer.name,
            onValueChange = { name -> onTrainerChange(trainer.copy(name)) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            NumberField(
                Modifier.weight(1f),
                label = "Visible ID",
                value = trainer.visibleId,
                onValueChange = { id -> onTrainerChange(trainer.copy(visibleId = id)) }
            )
            Spacer(Modifier.width(8.dp))
            NumberField(
                Modifier.weight(1f),
                label = "Secret ID",
                value = trainer.secretId,
                onValueChange = { id -> onTrainerChange(trainer.copy(secretId = id)) }
            )
        }
    }
}

@Composable
private inline fun TextField(
    modifier: Modifier,
    @Suppress("SameParameterValue") label: String,
    value: String,
    crossinline onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(text = label) },
        keyboardType = KeyboardType.Text
    )
}

@Composable
private inline fun NumberField(
    modifier: Modifier,
    label: String,
    value: Int,
    crossinline onValueChange: (Int) -> Unit
) {
    OutlinedTextField(
        modifier = modifier,
        value = value.toString(),
        onValueChange = {
            if (it.isBlank()) {
                onValueChange(0)
            } else if (it.all(Char::isDigit)) {
                onValueChange(it.toInt())
            }
        },
        label = { Text(text = label) },
        keyboardType = KeyboardType.Number
    )
}
