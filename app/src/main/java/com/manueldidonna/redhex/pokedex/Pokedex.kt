package com.manueldidonna.redhex.pokedex

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ArrowBack
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.redhex.common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Pokedex(pokedex: Pokedex, onClosePokedex: () -> Unit) {
    val entries = getPokedexEntries(pokedex)

    val mutateEntries = remember(entries) {
        { entry: PokedexEntry ->
            val newEntry = when {
                entry.isOwned -> entry.copy(isSeen = false, isOwned = false)
                entry.isSeen -> entry.copy(isSeen = true, isOwned = true)
                else -> entry.copy(isSeen = true, isOwned = false)
            }
            pokedex.setEntry(newEntry)
            entries[newEntry.speciesId - 1] = newEntry
        }
    }

    Stack(Modifier.fillMaxSize()) {
        TranslucentAppBar(
            title = "Pokedex",
            modifier = Modifier.zIndex(8f),
            navigationIcon = {
                IconButton(onClick = onClosePokedex) {
                    Icon(asset = Icons.TwoTone.ArrowBack)
                }
            }
        )
        if (entries.isEmpty()) return@Stack
        LazyColumnForIndexed(items = entries) { index, entry ->
            if (index == 0) {
                // TODO: BUG with lazy column contentPadding
                Spacer(Modifier.height(AppBarHeight))
            }
            PokedexEntry(entry = entry, mutateEntries)
            Divider()
        }
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

@Composable
private fun PokedexEntry(entry: PokedexEntry, onClick: (PokedexEntry) -> Unit) {
    ListItem(
        text = { Text(text = entry.name) },
        overlineText = { Text(text = "#${entry.speciesId}") },
        trailing = if (entry.isOwned) OwnedIcon else null,
        modifier = Modifier.clickable(onClick = { onClick(entry) }),
        icon = {
            Box(gravity = ContentGravity.Center, modifier = Modifier.size(40.dp)) {
                PokemonSprite(source = entry.source, enabled = entry.isSeen)
            }
        }
    )
}

private val OwnedIcon = @Composable {
    Icon(asset = Icons.TwoTone.CheckCircle, tint = MaterialTheme.colors.secondary)
}
