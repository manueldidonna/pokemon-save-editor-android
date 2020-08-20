package com.manueldidonna.redhex.details

import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.InnerPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.MutablePokemon
import com.manueldidonna.pk.core.valueOrNull
import com.manueldidonna.redhex.common.PokemonResourcesAmbient

interface PokemonDetailsEvents {
    fun goBackToPokemonList(pokemon: MutablePokemon)
}

@Composable
fun PokemonDetails(
    modifier: Modifier = Modifier,
    pokemon: MutablePokemon,
    listener: PokemonDetailsEvents,
) {
    Column(modifier) {
        TopAppBar(
            title = { Text(text = "Pokemon Editor") },
            navigationIcon = {
                IconButton(onClick = { listener.goBackToPokemonList(pokemon) }) {
                    Icon(Icons.TwoTone.ArrowBack)
                }
            }
        )
        val resources = PokemonResourcesAmbient.current
        val observablePokemon = remember { ObservablePokemon(pokemon) }
        ScrollableColumn(
            modifier = Modifier.fillMaxHeight(),
            contentPadding = InnerPadding(top = 16.dp, bottom = 16.dp)
        ) {
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
}

@Suppress("NOTHING_TO_INLINE")
@Composable
private inline fun DetailsDivider() {
    Divider(modifier = Modifier.padding(vertical = 8.dp))
}
