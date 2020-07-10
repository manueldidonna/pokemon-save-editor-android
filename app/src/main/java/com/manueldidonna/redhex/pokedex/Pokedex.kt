package com.manueldidonna.redhex.pokedex

import androidx.compose.Composable
import androidx.compose.frames.ModelList
import androidx.compose.remember
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.ContentGravity
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.graphics.ColorFilter
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.preferredHeight
import androidx.ui.layout.size
import androidx.ui.material.Divider
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.ListItem
import androidx.ui.material.MaterialTheme
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.CheckCircle
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.pk.core.getAllEntries
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpriteSize
import com.manueldidonna.redhex.common.SpriteSource
import com.manueldidonna.redhex.common.SpritesRetrieverAmbient
import com.manueldidonna.redhex.common.ui.LightColors
import com.manueldidonna.redhex.common.ui.ToolbarHeight
import com.manueldidonna.redhex.common.ui.TranslucentToolbar
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun Pokedex(modifier: Modifier = Modifier, pokedex: Pokedex) {
    val entries = remember {
        ModelList<Pokedex.Entry>().apply { addAll(pokedex.getAllEntries()) }
    }

    Column(modifier) {
        TranslucentToolbar(
            modifier = Modifier.preferredHeight(ToolbarHeight),
            horizontalArrangement = Arrangement.Center
        ) {
            val title: String = remember(entries) {
                val seenCount = entries.count { it.isSeen }
                if (seenCount != pokedex.pokemonCount) {
                    "${(seenCount.toDouble() / pokedex.pokemonCount * 100).toInt()}% Completed"
                } else {
                    val ownedCount = entries.count { it.isOwned }
                    "${(ownedCount.toDouble() / pokedex.pokemonCount * 100).toInt()}% Owned"
                }
            }
            Text(text = title)
        }
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

@Composable
private fun EntriesList(entries: List<Pokedex.Entry>, onEntryClick: (Pokedex.Entry) -> Unit) {
    val resources = PokemonResourcesAmbient.current.species
    val spritesRetriever = SpritesRetrieverAmbient.current
    LazyColumnItems(items = entries) { entry ->
        PokedexEntry(
            entry = entry,
            name = resources.getSpeciesById(entry.speciesId),
            source = spritesRetriever.getPokemonSprite(entry.speciesId, shiny = false),
            onClick = { onEntryClick(entry) }
        )
        Divider()
    }
}

private val OwnedIcon = @Composable {
    Icon(asset = Icons.TwoTone.CheckCircle, tint = MaterialTheme.colors.secondary)
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
                PokedexSprite(source = source, isSeen = entry.isSeen)
            }
        }
    )
}

@Composable
private fun PokedexSprite(source: SpriteSource, isSeen: Boolean) {
    var colorFilter: ColorFilter? = null
    if (!isSeen) {
        val emphasis = EmphasisAmbient.current.disabled
        colorFilter = ColorFilter.tint(emphasis.applyEmphasis(MaterialTheme.colors.onSurface))
    }
    CoilImage(
        modifier = PokemonSpriteSize,
        colorFilter = colorFilter,
        data = source.value
    )
}

@Composable
@Preview
private fun PreviewPokedexEntry() {
    MaterialTheme(colors = LightColors) {
        Column {
            val entries = listOf(
                Pokedex.Entry.neverSeen(25),
                Pokedex.Entry.onlySeen(25),
                Pokedex.Entry.owned(25)
            )
            for (entry in entries) {
                PokedexEntry(
                    entry = entry,
                    name = "Pikachu",
                    source = SpriteSource(""),
                    onClick = {}
                )
            }
        }
    }
}