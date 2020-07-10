package com.manueldidonna.redhex.details

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.ContentGravity
import androidx.ui.foundation.Text
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.input.KeyboardType
import androidx.ui.input.TextFieldValue
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.text.TextRange
import androidx.ui.text.font.FontWeight
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Trainer
import com.manueldidonna.pk.core.Version
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSprite
import com.manueldidonna.redhex.common.SpriteSource
import com.manueldidonna.redhex.common.SpritesRetrieverAmbient
import com.manueldidonna.redhex.common.ui.ThemedDialog
import kotlin.math.roundToInt

@Composable
fun PokemonGeneral(pokemon: MutablePokemon) {
    val resources = PokemonResourcesAmbient.current
    var speciesId: Int by state { pokemon.speciesId }
    val speciesName: String = remember(speciesId) {
        resources.species.getSpeciesById(speciesId)
    }
    var isShiny by state { pokemon.isShiny }
    Species(pokemon.version, speciesId, speciesName, isShiny) {
        pokemon.mutator
            .speciesId(it)
            .level(pokemon.level)
            .nickname(resources.species.getSpeciesById(it), ignoreCase = true)
        speciesId = pokemon.speciesId
    }
    Divider()
    Shiny(
        isShiny = isShiny,
        onChange = {
            pokemon.mutator.shiny(it)
            isShiny = pokemon.isShiny
        }
    )
    Divider()
    Experience(pokemon)
    Divider()
    var trainer: Trainer by state { pokemon.trainer }
    Trainer(trainer = trainer) {
        pokemon.mutator.trainer(it, ignoreNameCase = false)
        trainer = pokemon.trainer
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

    var showSpeciesDialog by state { false }

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
            LazyColumnItems(items = resources.getAllSpecies(version)
                .drop(1) // TODO: manage empty species id
                .mapIndexed { index, s -> Pair(index, s) }
            ) {
                ListItem(
                    text = it.second,
                    onClick = {
                        onSpeciesChange(it.first + 1)
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
    var level by state { pokemon.level }
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
private fun Trainer(
    trainer: Trainer,
    onTrainerChange: (Trainer) -> Unit
) {
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
private fun TextField(
    modifier: Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var selection by state { TextRange.Zero }
    OutlinedTextField(
        modifier = modifier,
        value = TextFieldValue(text = value, selection = selection),
        onValueChange = {
            selection = it.selection
            onValueChange(it.text)
        },
        label = { Text(text = label) },
        keyboardType = KeyboardType.Text
    )
}

@Composable
private fun NumberField(
    modifier: Modifier,
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    var selection by state { TextRange.Zero }
    OutlinedTextField(
        modifier = modifier,
        value = TextFieldValue(text = value.toString(), selection = selection),
        onValueChange = {
            if (it.text.all(Char::isDigit)) {
                selection = it.selection
                onValueChange(it.text.ifEmpty { "0" }.toInt())
            }
        },
        label = { Text(text = label) },
        keyboardType = KeyboardType.Number
    )
}
