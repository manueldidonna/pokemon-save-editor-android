package com.manueldidonna.redhex.details

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.input.KeyboardType
import androidx.ui.layout.*
import androidx.ui.material.Divider
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Slider
import androidx.ui.material.ripple.ripple
import androidx.ui.text.font.FontWeight
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.pk.core.info.getExperienceGroup
import com.manueldidonna.pk.core.info.getExperiencePoints
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpritesRetrieverAmbient
import com.manueldidonna.redhex.common.pokemon.pokemonSpriteSize
import com.manueldidonna.redhex.common.ui.DialogItem
import com.manueldidonna.redhex.common.ui.DialogMenu
import com.manueldidonna.redhex.dividerColor
import dev.chrisbanes.accompanist.coil.CoilImage
import java.io.File
import kotlin.math.roundToInt

private class GeneralState(speciesId: Int, level: Float, experiencePoints: Int) {
    var speciesId by mutableStateOf(speciesId, areEquivalent = StructurallyEqual)
    var level by mutableStateOf(level, areEquivalent = StructurallyEqual)
    var experiencePoints by mutableStateOf(experiencePoints, areEquivalent = StructurallyEqual)
}

@Composable
fun PokemonGeneral(pokemon: MutablePokemon, pokedex: Pokedex) {
    val state = remember {
        GeneralState(pokemon.speciesId, pokemon.level.toFloat(), pokemon.experiencePoints)
    }
    SpeciesEditorField(pokemon, pokedex, state)
    Divider(color = dividerColor())
    ExperienceEditorField(pokemon, state)
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
            Spacer(modifier = Modifier.preferredWidth(8.dp))
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
                        // TODO: uppercase name is a gen 1-3 detail. Abstract it
                        .nickname(it.second.toUpperCase())
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
    var experienceFieldValue by stateFor(state.experiencePoints) {
        TextFieldValue(text = state.experiencePoints.toString())
    }

    val maxExperience: Int = remember(state.speciesId) {
        getExperiencePoints(100, getExperienceGroup(state.speciesId))
    }

    fun updateExperience() = state.run {
        level = pokemon.level.toFloat()
        experiencePoints = pokemon.experiencePoints
    }

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(
            verticalGravity = Alignment.CenterVertically,
            modifier = Modifier.preferredHeight(48.dp)
        ) {
            Text(
                text = "Experience",
                modifier = Modifier.padding(end = 24.dp),
                style = MaterialTheme.typography.subtitle1
            )
            TextField(
                modifier = Modifier.weight(1f),
                value = experienceFieldValue,
                onValueChange = { newValue ->
                    if (newValue.text.isEmpty()) {
                        pokemon.mutator.experiencePoints(0)
                        experienceFieldValue = newValue.copy(text = "0")
                        updateExperience()
                    } else if (newValue.text.all(Char::isDigit)) {
                        val coercedExp = newValue.text.toInt().coerceAtMost(maxExperience)
                        pokemon.mutator.experiencePoints(coercedExp)
                        experienceFieldValue = newValue.copy(text = coercedExp.toString())
                        updateExperience()
                    }
                },
                keyboardType = KeyboardType.Number,
                textStyle = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium)
            )
        }
        Row(
            verticalGravity = Alignment.CenterVertically,
            modifier = Modifier.preferredHeight(48.dp)
        ) {
            Text(
                text = "Level",
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = state.level.roundToInt().toString(),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Medium)
            )
            Slider(
                modifier = Modifier.weight(1f),
                value = state.level,
                onValueChange = { state.level = it },
                valueRange = 1f..100f,
                onValueChangeEnd = {
                    pokemon.mutator.level(state.level.roundToInt())
                    updateExperience()
                }
            )
        }
    }
}