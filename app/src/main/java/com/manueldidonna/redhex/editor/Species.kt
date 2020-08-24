package com.manueldidonna.redhex.editor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Version
import com.manueldidonna.redhex.common.*
import com.manueldidonna.redhex.common.ThemedDialog

@Composable
fun ModifySpecies(
    version: Version,
    speciesId: Int,
    nickname: String,
    isShiny: Boolean,
    onSpeciesChange: (id: Int) -> Unit,
    onNicknameChange: (String) -> Unit,
    onShinyChange: (Boolean) -> Unit,
) {
    var changeSpecies by rememberMutableState { false }
    if (changeSpecies) {
        ChangeSpeciesDialog(
            version = version,
            onDismissRequest = { changeSpecies = false },
            onSpeciesChange = onSpeciesChange
        )
    }
    Column {
        Row(
            verticalGravity = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = { changeSpecies = true })
                .padding(horizontal = 8.dp)
        ) {
            Box(gravity = ContentGravity.Center, modifier = Modifier.size(48.dp)) {
                PokemonSprite(source = spriteSource(speciesId, shinySprite = isShiny))
            }
            Text(
                text = speciesName(speciesId),
                style = MaterialTheme.typography.body1,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            IconButton(onClick = { onShinyChange(!isShiny) }) {
                val tint = if (isShiny) MaterialTheme.colors.secondary else contentColor()
                Icon(asset = Icons.TwoTone.Star, tint = tint)
            }
        }
        Spacer(Modifier.height(8.dp))
        TextField(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
            value = nickname,
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

@Composable
private fun ChangeSpeciesDialog(
    version: Version,
    onDismissRequest: () -> Unit,
    onSpeciesChange: (speciesId: Int) -> Unit,
) {
    val resources = PokemonResourcesAmbient.current.species
    ThemedDialog(onDismissRequest = onDismissRequest) {
        LazyColumnForIndexed(items = resources.getAllSpecies(version)) { index, name ->
            ListItem(
                text = { Text(text = name) },
                modifier = Modifier.clickable(onClick = {
                    onSpeciesChange(index + 1)
                    onDismissRequest()
                })
            )
        }
    }
}
