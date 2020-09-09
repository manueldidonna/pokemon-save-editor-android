package com.manueldidonna.redhex.editor

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.contentColor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Version
import com.manueldidonna.redhex.common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ModifySpecies(
    version: Version,
    species: ObservablePokemon.Species,
    onSpeciesIdChange: (id: Int) -> Unit,
    onNicknameChange: (String) -> Unit,
    onShinyChange: (Boolean) -> Unit,
) {
    var changeSpecies by rememberMutableState { false }
    if (changeSpecies) {
        SpeciesSelectorDialog(
            version = version,
            onDismissRequest = { changeSpecies = false },
            onSelect = onSpeciesIdChange
        )
    }
    Column {
        Row(
            verticalGravity = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = { changeSpecies = true })
                .padding(horizontal = 8.dp)
        ) {
            PokemonSprite(
                source = spriteSource(species.id, shinySprite = species.isShiny),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = speciesName(species.id),
                style = MaterialTheme.typography.body1,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            IconButton(onClick = { onShinyChange(!species.isShiny) }) {
                val tint = if (species.isShiny) MaterialTheme.colors.secondary else contentColor()
                Icon(asset = Icons.TwoTone.Star, tint = tint)
            }
        }
        Spacer(Modifier.height(8.dp))
        TextField(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
            value = species.nickname,
            onValueChange = onNicknameChange,
            label = { Text(text = "Nickname") },
            keyboardType = KeyboardType.Text
        )
    }
}

@Composable
@Stable
private fun spriteSource(speciesId: Int, shinySprite: Boolean): SpriteSource {
    val spritesRetriever = SpritesRetrieverAmbient.current
    return spritesRetriever.getPokemonSprite(speciesId, shiny = shinySprite)
}

@Composable
@Stable
private fun speciesName(speciesId: Int): String {
    val resources = PokemonResourcesAmbient.current
    return resources.species.getSpeciesById(speciesId)
}

@Immutable
private data class SpeciesIdWithName(val id: Int, val name: String)

@Composable
private fun SpeciesSelectorDialog(
    version: Version,
    onDismissRequest: () -> Unit,
    onSelect: (speciesId: Int) -> Unit,
) {
    val resources = PokemonResourcesAmbient.current.species
    val (species, updateSpecies) = rememberMutableState<List<SpeciesIdWithName>> { emptyList() }
    launchInComposition {
        updateSpecies(withContext(Dispatchers.IO) {
            resources
                .getAllSpecies(version)
                .mapIndexed { index, name -> SpeciesIdWithName(index + 1, name) }
                .sortedBy { it.name }
        })
    }
    if (species.isEmpty()) return
    ThemedDialog(onDismissRequest = onDismissRequest) {
        LazyColumnFor(items = species) { entity ->
            ListItem(
                text = { Text(text = entity.name) },
                modifier = Modifier.clickable(onClick = {
                    onSelect(entity.id)
                    onDismissRequest()
                })
            )
        }
    }
}
