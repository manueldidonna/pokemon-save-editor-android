package com.manueldidonna.redhex.pokedex

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpriteSize
import com.manueldidonna.redhex.common.SpriteSource
import com.manueldidonna.redhex.common.SpritesRetrieverAmbient
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Pokedex(modifier: Modifier = Modifier, pokedex: Pokedex) {
    val entries: SnapshotStateList<PokedexEntry> = getPokedexEntries(pokedex)

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

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(text = pokedexCompletionStatus(pokedex.pokemonCount, entries))
            }
        )
        if (entries.isEmpty()) return@Column
        LazyColumnFor(items = entries) { entry ->
            PokedexEntry(entry = entry, mutateEntries)
            Divider()
        }
    }
}

@Stable
@Composable
private fun pokedexCompletionStatus(pokedexCount: Int, entries: List<PokedexEntry>): String {
    val seenCount = entries.count { it.isSeen }
    return if (seenCount != pokedexCount) {
        "${(seenCount.toDouble() / pokedexCount * 100).toInt()}% Pokemon Seen"
    } else {
        val ownedCount = entries.count { it.isOwned }
        "${(ownedCount.toDouble() / pokedexCount * 100).toInt()}% Pokemon Owned"
    }
}

@Stable
@Composable
private fun getPokedexEntries(pokedex: Pokedex): SnapshotStateList<PokedexEntry> {
    val mutableEntries = remember { mutableStateListOf<PokedexEntry>() }
    val resources = PokemonResourcesAmbient.current.species
    val spritesRetriever = SpritesRetrieverAmbient.current
    launchInComposition {
        withContext(Dispatchers.IO) {
            val entries = PokedexEntry.getAllFromPokedex(pokedex, spritesRetriever, resources)
            mutableEntries.addAll(entries)
        }
    }
    return mutableEntries
}

private val OwnedIcon = @Composable {
    Icon(asset = Icons.TwoTone.CheckCircle, tint = MaterialTheme.colors.secondary)
}

@Composable
private fun PokedexEntry(entry: PokedexEntry, onClick: (PokedexEntry) -> Unit) {
    ListItem(
        text = { Text(text = entry.name) },
        overlineText = { Text(text = "#${entry.speciesId}") },
        trailing = if (entry.isOwned) OwnedIcon else null,
        modifier = Modifier.clickable(onClick = { onClick(entry) }),
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
