package com.manueldidonna.redhex.editor

import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.InnerPadding
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.redhex.common.AppBarHeight
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.TranslucentAppBar

interface PokemonEditorEvents {
    fun closeEditor()
    fun applyPokemonChanges(pokemon: Pokemon)
}

@Composable
fun PokemonEditor(
    modifier: Modifier = Modifier,
    pokemon: MutablePokemon,
    listener: PokemonEditorEvents,
) {
    val observablePokemon = remember { ObservablePokemon(pokemon) }
    Stack(modifier.fillMaxSize()) {
        TranslucentAppBar(
            modifier = Modifier.zIndex(8f),
            title = "Pokemon Editor",
            navigationIcon = {
                IconButton(onClick = listener::closeEditor) {
                    Icon(Icons.TwoTone.ArrowBack)
                }
            }
        )
        EditorFields(
            observablePokemon = observablePokemon,
            modifier = Modifier.fillMaxSize(),
            contentPadding = InnerPadding(top = 16.dp + AppBarHeight, bottom = 32.dp + 48.dp)
        )
        ExtendedFloatingActionButton(
            modifier = Modifier.gravity(Alignment.BottomEnd).padding(16.dp),
            text = { Text("APPLY CHANGES") },
            icon = { Icon(asset = Icons.TwoTone.Save) },
            onClick = { listener.applyPokemonChanges(pokemon) }
        )
    }
}

@Composable
private fun EditorFields(
    modifier: Modifier,
    observablePokemon: ObservablePokemon,
    contentPadding: InnerPadding,
) {
    val resources = PokemonResourcesAmbient.current
    val version = observablePokemon.version
    ScrollableColumn(modifier = modifier, contentPadding = contentPadding) {
        ModifySpecies(
            version = version,
            species = observablePokemon.species,
            onSpeciesIdChange = {
                observablePokemon.mutator
                    .speciesId(it)
                    .level(observablePokemon.level)
                    .nickname(resources.species.getSpeciesById(it), ignoreCase = true)
            },
            onNicknameChange = observablePokemon.mutator::nickname,
            onShinyChange = observablePokemon.mutator::shiny
        )

        DetailsDivider()
        ModifyExperience(
            level = observablePokemon.level,
            experience = observablePokemon.experiencePoints,
            onLevelChange = observablePokemon.mutator::level,
            onExperienceChange = observablePokemon.mutator::experiencePoints
        )

        DetailsDivider()
        ModifyMoves(
            version = version,
            moves = observablePokemon.moves,
            onMoveChange = observablePokemon.mutator::move
        )

        val pokerus = observablePokemon.pokerus
        if (pokerus != null) {
            DetailsDivider()
            ModifyPokerus(
                strain = pokerus.strain,
                days = pokerus.days,
                onChange = observablePokemon.mutator::pokerus
            )
        }

        val friendship = observablePokemon.friendship
        if (friendship != null) {
            DetailsDivider()
            ModifyFriendship(friendship, observablePokemon.mutator::friendship)
        }

        val wrappedForm = observablePokemon.wrappedForm
        if (wrappedForm.form != null) {
            DetailsDivider()
            ModifyForm(
                version = version,
                form = wrappedForm,
                onFormChange = observablePokemon.mutator::form
            )
        }

        val trainer = observablePokemon.trainer
        DetailsDivider()
        ModifyTrainer(
            name = trainer.name,
            visibleId = trainer.visibleId,
            secretId = trainer.secretId,
            gender = trainer.gender,
            onChange = { observablePokemon.mutator.trainer(it, ignoreNameCase = false) }
        )
    }
}

@Suppress("NOTHING_TO_INLINE")
@Composable
private inline fun DetailsDivider() {
    Divider(modifier = Modifier.padding(vertical = 8.dp))
}
