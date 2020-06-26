package com.manueldidonna.redhex.pokedex

import androidx.compose.Composable
import androidx.compose.frames.ModelList
import androidx.compose.remember
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.zIndex
import androidx.ui.foundation.*
import androidx.ui.graphics.ColorFilter
import androidx.ui.layout.*
import androidx.ui.material.Divider
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.ListItem
import androidx.ui.material.MaterialTheme
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.Done
import androidx.ui.res.imageResource
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.pk.core.getAllEntries
import com.manueldidonna.redhex.R
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpriteSize
import com.manueldidonna.redhex.common.SpriteSource
import com.manueldidonna.redhex.common.SpritesRetrieverAmbient
import com.manueldidonna.redhex.common.ui.TranslucentToolbar
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun Pokedex(modifier: Modifier = Modifier, pokedex: Pokedex) {
    val entries = remember {
        ModelList<Pokedex.Entry>().apply { addAll(pokedex.getAllEntries()) }
    }

    Stack(modifier) {
        TranslucentToolbar(
            modifier = Modifier.preferredHeight(56.dp).gravity(Alignment.TopCenter).zIndex(8f),
            horizontalArrangement = Arrangement.Center
        ) {
            val percentage = entries.count { it.isSeen }.toDouble() / pokedex.pokemonCount * 100
            Text(text = "${percentage.toInt()}% Completed")
        }
        VerticalScroller {
            Spacer(modifier = Modifier.preferredHeight(56.dp))
            EntriesList(
                entries = entries,
                onEntryClick = { entry ->
                    val newEntry = when {
                        entry.isOwned -> Pokedex.Entry.neverSeen(entry.speciesId)
                        entry.isSeen -> Pokedex.Entry.owned(entry.speciesId)
                        else -> Pokedex.Entry.onlySeen(entry.speciesId)
                    }
                    pokedex.setEntry(newEntry)
                    entries[newEntry.speciesId - 1] = newEntry
                }
            )
        }
    }
}

@Composable
private fun EntriesList(entries: List<Pokedex.Entry>, onEntryClick: (Pokedex.Entry) -> Unit) {
    val resources = PokemonResourcesAmbient.current.species
    val spritesRetriever = SpritesRetrieverAmbient.current
    for (entry in entries) {
        PokedexEntry(
            entry = entry,
            name = resources.getSpeciesById(entry.speciesId),
            source = spritesRetriever.getPokemonSprite(entry.speciesId),
            onClick = { onEntryClick(entry) }
        )
        Divider()
    }
}

private val OwnedIcon = @Composable {
    Icon(asset = Icons.TwoTone.Done, tint = MaterialTheme.colors.secondary)
}

@Composable
private fun PokedexEntry(
    entry: Pokedex.Entry,
    name: String,
    source: SpriteSource,
    onClick: () -> Unit
) {
    ListItem(
        text = { Text(text = name) },
        overlineText = { Text(text = "#${entry.speciesId}") },
        trailing = if (entry.isOwned) OwnedIcon else null,
        onClick = onClick,
        icon = {
            Box(gravity = ContentGravity.Center, modifier = Modifier.size(40.dp)) {
                PokemonSprite(source = if (entry.isSeen) source.value else null)
            }
        }
    )
}

@Composable
private fun PokemonSprite(source: Any?) {
    // placeholder for empty slot
    if (source == null) {
        val emphasis = EmphasisAmbient.current.disabled
        Image(
            modifier = PokemonSpriteSize,
            colorFilter = ColorFilter.tint(emphasis.applyEmphasis(MaterialTheme.colors.onSurface)),
            asset = imageResource(R.drawable.pokeball_s)
        )
    } else {
        CoilImage(data = source, modifier = PokemonSpriteSize)
    }
}
