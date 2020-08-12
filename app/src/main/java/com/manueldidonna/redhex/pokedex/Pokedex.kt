package com.manueldidonna.redhex.pokedex

import androidx.compose.foundation.Box
import androidx.compose.foundation.ContentGravity
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.Divider
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.material.icons.twotone.Visibility
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpriteSize
import com.manueldidonna.redhex.common.SpriteSource
import com.manueldidonna.redhex.common.SpritesRetrieverAmbient
import com.manueldidonna.redhex.common.ui.ToolbarHeight
import com.manueldidonna.redhex.common.ui.TranslucentToolbar
import dev.chrisbanes.accompanist.coil.CoilImage

private val EntriesListContentPadding = InnerPadding(top = ToolbarHeight)

@Composable
fun Pokedex(modifier: Modifier = Modifier, pokedex: Pokedex) {
    val resources = PokemonResourcesAmbient.current.species
    val spritesRetriever = SpritesRetrieverAmbient.current
    val entries = remember {
        PokedexEntry
            .getAllFromPokedex(pokedex, spritesRetriever, resources)
            .toMutableStateList()
    }

    val mutateEntries: (PokedexEntry) -> Unit = remember(entries) {
        { entry ->
            val newEntry = when {
                entry.isOwned -> entry.copy(isSeen = false, isOwned = false)
                entry.isSeen -> entry.copy(isSeen = true, isOwned = true)
                else -> entry.copy(isSeen = true, isOwned = false)
            }
            pokedex.setEntry(newEntry)
            entries[newEntry.speciesId - 1] = newEntry
        }
    }

    Stack(modifier) {
        TranslucentToolbar(
            modifier = Modifier.preferredHeight(ToolbarHeight).zIndex(8f),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = toolbarTitle(pokedex.pokemonCount, entries.count { it.isSeen }, ))
        }
        LazyColumnFor(items = entries, contentPadding = EntriesListContentPadding) { entry ->
            PokedexEntry(entry = entry, mutateEntries)
            Divider()
        }
    }
}

@Stable
@Composable
private fun toolbarTitle(pokedexCount: Int, seenCount: Int): String {
    return "${(seenCount.toDouble() / pokedexCount * 100).toInt()}% Completed"
}

private val OwnedIcon = @Composable {
    Icon(asset = Icons.TwoTone.CheckCircle, tint = MaterialTheme.colors.secondary)
}

private val SeenIcon = @Composable {
    Icon(asset = Icons.TwoTone.Visibility, tint = MaterialTheme.colors.secondary)
}

@Composable
private fun PokedexEntry(entry: PokedexEntry, onClick: (PokedexEntry) -> Unit) {
    ListItem(
        text = { Text(text = entry.name) },
        overlineText = { Text(text = "#${entry.speciesId}") },
        trailing = when {
            entry.isOwned -> OwnedIcon
            entry.isSeen -> SeenIcon
            else -> null
        },
        onClick = { onClick(entry) },
        icon = {
            Box(gravity = ContentGravity.Center, modifier = Modifier.size(40.dp)) {
                PokedexSprite(source = entry.source, isSeen = entry.isSeen)
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
