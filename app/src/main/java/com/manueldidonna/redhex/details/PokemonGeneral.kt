package com.manueldidonna.redhex.details

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.AdapterList
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.Text
import androidx.ui.foundation.TextFieldValue
import androidx.ui.input.KeyboardType
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.ripple.ripple
import androidx.ui.text.AnnotatedString
import androidx.ui.text.SpanStyle
import androidx.ui.text.TextRange
import androidx.ui.text.font.FontWeight
import androidx.ui.text.withStyle
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

private class GeneralState(speciesId: Int, level: Int, experiencePoints: Int) {
    var speciesId by mutableStateOf(speciesId, areEquivalent = StructurallyEqual)
    var level by mutableStateOf(level, areEquivalent = StructurallyEqual)
    var experiencePoints by mutableStateOf(experiencePoints, StructurallyEqual)
}

@Composable
fun PokemonGeneral(pokemon: MutablePokemon, pokedex: Pokedex) {
    val state = remember {
        GeneralState(pokemon.speciesId, pokemon.level, pokemon.experiencePoints)
    }
    SpeciesEditorField(pokemon, pokedex, state)
    Divider(color = dividerColor())
    ExperienceEditorField(pokemon, state)
    Divider(color = dividerColor())
    TrainerEditorField(pokemon)
}

@Composable
private fun SpeciesEditorField(pokemon: MutablePokemon, pokedex: Pokedex, state: GeneralState) {
    val species = PokemonResourcesAmbient.current.species
    val spritesRetriever = PokemonSpritesRetrieverAmbient.current
    val spriteSource = remember(state.speciesId) {
        File(spritesRetriever.getSpritesPathFromId(state.speciesId))
    }

    var showSpeciesDialog by state { false }

    Clickable(onClick = { showSpeciesDialog = true }, modifier = Modifier.ripple()) {
        Row(
            verticalGravity = Alignment.CenterVertically,
            modifier = Modifier.preferredHeight(56.dp).fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.preferredWidth(16.dp))
            CoilImage(
                data = spriteSource,
                modifier = Modifier.pokemonSpriteSize()
            )
            Text(
                text = species.getSpeciesById(state.speciesId),
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
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
                    state.run {
                        speciesId = pokemon.speciesId
                        experiencePoints = pokemon.experiencePoints
                    }
                }
            }
        }
}

@Composable
private fun ExperienceEditorField(pokemon: MutablePokemon, state: GeneralState) {
    Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(8.dp))
        val valueStyle = SpanStyle(
            fontWeight = FontWeight.Medium,
            color = EmphasisAmbient.current.high.emphasize(MaterialTheme.colors.onSurface)
        )
        Text(
            text = AnnotatedString {
                append("Level ")
                withStyle(valueStyle) {
                    append(state.level.toString())
                }
                append(" with ")
                withStyle(valueStyle) {
                    append(state.experiencePoints.toString())
                }
                append(" experience points")
            },
            style = MaterialTheme.typography.body1,
            color = EmphasisAmbient.current.medium.emphasize(MaterialTheme.colors.onSurface)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = state.level.toFloat(),
            valueRange = 1f..100f,
            onValueChange = {
                state.level = it.roundToInt()
                pokemon.mutator.level(state.level)
                state.experiencePoints = pokemon.experiencePoints
            }
        )
    }
}

@Composable
private fun TrainerEditorField(pokemon: MutablePokemon) {
    var trainer: Trainer by state { pokemon.trainer }
    var nameSelection by state { TextRange(0, 0) }
    var visibleIdSelection by state { TextRange(0, 0) }
    var secretIdSelection by state { TextRange(0, 0) }
    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = "Trainer Info",
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium)
        )
        Spacer(modifier = Modifier.height(24.dp))
        FilledTextField(
            modifier = Modifier.fillMaxWidth(),
            value = TextFieldValue(text = trainer.name, selection = nameSelection),
            onValueChange = {
                pokemon.mutator.trainer(trainer.copy(name = it.text))
                nameSelection = it.selection
                trainer = pokemon.trainer
            },
            label = { Text(text = "Name") },
            keyboardType = KeyboardType.Text
        )
        Spacer(modifier = Modifier.height(24.dp))
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
