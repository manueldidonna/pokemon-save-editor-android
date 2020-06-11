package com.manueldidonna.redhex.details

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.AdapterList
import androidx.ui.foundation.Text
import androidx.ui.foundation.TextFieldValue
import androidx.ui.foundation.clickable
import androidx.ui.input.KeyboardType
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.text.TextRange
import androidx.ui.text.font.FontWeight
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.pk.core.Trainer
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpritesRetrieverAmbient
import com.manueldidonna.redhex.common.pokemon.pokemonSpriteSize
import com.manueldidonna.redhex.common.ui.DialogItem
import com.manueldidonna.redhex.common.ui.DialogMenu
import com.manueldidonna.redhex.dividerColor
import dev.chrisbanes.accompanist.coil.CoilImage
import java.io.File
import kotlin.math.roundToInt

@Composable
private val LabelTextStyle
    get() = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium)

@Composable
fun PokemonGeneral(pokemon: MutablePokemon, pokedex: Pokedex) {
    SpeciesEditorField(pokemon, pokedex)
    Divider(color = dividerColor())
    ExperienceEditorField(pokemon)
    Divider(color = dividerColor())
    TrainerEditorField(pokemon)
}

@Composable
private fun SpeciesEditorField(pokemon: MutablePokemon, pokedex: Pokedex) {
    var speciesId by state { pokemon.speciesId }
    val species = PokemonResourcesAmbient.current.species
    val spritesRetriever = PokemonSpritesRetrieverAmbient.current
    val spriteSource = remember(speciesId) {
        File(spritesRetriever.getSpritesPathFromId(speciesId))
    }

    var showSpeciesDialog by state { false }
    Row(
        verticalGravity = Alignment.CenterVertically,
        modifier = Modifier
            .preferredHeight(56.dp)
            .fillMaxWidth()
            .clickable(onClick = { showSpeciesDialog = true })
    ) {
        Spacer(modifier = Modifier.preferredWidth(16.dp))
        CoilImage(
            data = spriteSource,
            modifier = Modifier.pokemonSpriteSize()
        )
        Text(
            text = species.getSpeciesById(speciesId),
            style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
    if (showSpeciesDialog)
        DialogMenu(dismiss = { showSpeciesDialog = false }) {
            AdapterList(data = species.getAllSpecies()
                .drop(1) // TODO: manage empty species id
                .mapIndexed { index, s -> Pair(index, s) }
            ) {
                DialogItem(text = it.second) {
                    pokemon.mutator
                        .speciesId(it.first + 1)
                        .level(pokemon.level)
                        .nickname(it.second, ignoreCase = true)
                    pokedex.setEntry(Pokedex.Entry.owned(it.first + 1))
                    speciesId = pokemon.speciesId
                }
            }
        }
}

@Composable
private fun ExperienceEditorField(pokemon: MutablePokemon) {
    var level by state { pokemon.level }
    Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)) {
        Row {
            Text(text = "Level", style = LabelTextStyle, modifier = Modifier.weight(1f))
            Text(
                text = level.toString(),
                style = MaterialTheme.typography.body1,
                color = EmphasisAmbient.current.medium.applyEmphasis(MaterialTheme.colors.onSurface)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
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
private fun TrainerEditorField(pokemon: MutablePokemon) {
    var trainer: Trainer by state { pokemon.trainer }
    var nameSelection by state { TextRange(0, 0) }
    var visibleIdSelection by state { TextRange(0, 0) }
    var secretIdSelection by state { TextRange(0, 0) }
    Column(modifier = Modifier.padding(16.dp)) {
        FilledTextField(
            modifier = Modifier.fillMaxWidth(),
            value = TextFieldValue(text = trainer.name, selection = nameSelection),
            onValueChange = {
                pokemon.mutator.trainer(trainer.copy(name = it.text))
                nameSelection = it.selection
                trainer = pokemon.trainer
            },
            label = { Text(text = "Trainer Name") },
            keyboardType = KeyboardType.Text
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            FilledTextField(
                modifier = Modifier.weight(1f),
                value = TextFieldValue(
                    text = if (trainer.visibleId == 0u) "" else trainer.visibleId.toString(),
                    selection = visibleIdSelection
                ),
                onValueChange = {
                    if (it.text.all(Char::isDigit)) {
                        visibleIdSelection = it.selection
                        pokemon.mutator
                            .trainer(trainer.copy(visibleId = it.text.ifEmpty { "0" }.toUInt()))
                        trainer = pokemon.trainer
                    }
                },
                label = { Text(text = "Visible ID") },
                keyboardType = KeyboardType.Number
            )
            Spacer(modifier = Modifier.width(16.dp))
            FilledTextField(
                modifier = Modifier.weight(1f),
                value = TextFieldValue(
                    text = if (trainer.secretId == 0u) "" else trainer.secretId.toString(),
                    selection = secretIdSelection
                ),
                onValueChange = {
                    if (it.text.all(Char::isDigit)) {
                        secretIdSelection = it.selection
                        pokemon.mutator
                            .trainer(trainer.copy(secretId = it.text.ifEmpty { "0" }.toUInt()))
                        trainer = pokemon.trainer
                    }
                },
                label = { Text(text = "Secret ID") },
                keyboardType = KeyboardType.Number
            )
        }
    }
}
