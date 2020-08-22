package com.manueldidonna.redhex.editor

import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.InnerPadding
import androidx.compose.foundation.layout.Stack
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.onPositioned
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.valueOrNull
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.rememberMutableState

interface PokemonEditorEvents {
    fun goBackToPokemonList()
    fun savePokemon(pokemon: Pokemon)
}

@Composable
fun PokemonEditor(
    modifier: Modifier = Modifier,
    pokemon: MutablePokemon,
    listener: PokemonEditorEvents,
) {
    val observablePokemon = remember { ObservablePokemon(pokemon) }
    val (appBarHeight, setAppBarHeight) = rememberMutableState { 0.dp }
    val density = DensityAmbient.current
    Stack(modifier.fillMaxSize()) {
        TopAppBar(
            modifier = Modifier
                .gravity(Alignment.TopStart)
                .onPositioned { setAppBarHeight(with(density) { it.size.height.toDp() }) },
            title = { Text(text = "Pokemon Editor") },
            navigationIcon = {
                IconButton(onClick = listener::goBackToPokemonList) {
                    Icon(Icons.TwoTone.ArrowBack)
                }
            }
        )
        if (appBarHeight > 0.dp) {
            EditorFields(
                observablePokemon = observablePokemon,
                modifier = Modifier.matchParentSize().padding(top = appBarHeight),
                contentPadding = InnerPadding(top = 16.dp, bottom = 32.dp + 48.dp)
            )
        }
        ExtendedFloatingActionButton(
            modifier = Modifier.gravity(Alignment.BottomEnd).padding(16.dp),
            text = { Text("Save Pokemon")},
            icon = { Icon(asset = Icons.TwoTone.Save)},
            onClick = { listener.savePokemon(pokemon) }
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
    ScrollableColumn(modifier = modifier, contentPadding = contentPadding) {
        ModifySpecies(
            version = observablePokemon.version,
            speciesId = observablePokemon.speciesId,
            nickname = observablePokemon.nickname,
            isShiny = observablePokemon.isShiny,
            onSpeciesChange = {
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
            version = observablePokemon.version,
            moves = observablePokemon.moves,
            onMoveChange = observablePokemon.mutator::move
        )

        val pokerus = observablePokemon.pokerus.valueOrNull()
        if (pokerus != null) {
            DetailsDivider()
            ModifyPokerus(
                strain = pokerus.strain,
                days = pokerus.days,
                onChange = observablePokemon.mutator::pokerus
            )
        }

        val friendship = observablePokemon.friendship.valueOrNull()
        if (friendship != null) {
            DetailsDivider()
            ModifyFriendship(friendship, observablePokemon.mutator::friendship)
        }

        val wrappedForm = observablePokemon.wrappedForm
        if (wrappedForm.form != null) {
            DetailsDivider()
            ModifyForm(
                version = observablePokemon.version,
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
