package com.manueldidonna.redhex.pokedex

import androidx.compose.Composable
import androidx.compose.frames.ModelList
import androidx.compose.remember
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.clickable
import androidx.ui.graphics.ColorFilter
import androidx.ui.layout.*
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.MaterialTheme
import androidx.ui.res.imageResource
import androidx.ui.text.font.FontWeight
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.pk.core.getAllEntries
import com.manueldidonna.redhex.R
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpriteSize
import com.manueldidonna.redhex.common.SpriteSource
import com.manueldidonna.redhex.common.SpritesRetrieverAmbient
import com.manueldidonna.redhex.common.ui.Label
import com.manueldidonna.redhex.common.ui.TranslucentToolbar
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun Pokedex(modifier: Modifier = Modifier, pokedex: Pokedex) {
    val species = PokemonResourcesAmbient.current.species
    val entries = remember {
        ModelList<Pokedex.Entry>().apply { addAll(pokedex.getAllEntries()) }
    }

    val spritesRetriever = SpritesRetrieverAmbient.current

    Stack(modifier) {
        VerticalScroller {
            entries.forEach { entry ->
                if (entry.speciesId == 1)
                    Spacer(modifier = Modifier.preferredHeight(56.dp))
                PokedexEntry(
                    species = species.getSpeciesById(entry.speciesId),
                    spriteSource = spritesRetriever.getPokemonSprite(entry.speciesId),
                    isSeen = entry.isSeen,
                    isOwned = entry.isOwned,
                    id = entry.speciesId,
                    onValueChange = {
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
        TranslucentToolbar(
            modifier = Modifier.preferredHeight(56.dp).gravity(Alignment.TopCenter),
            horizontalArrangement = Arrangement.Center
        ) {
            val seenPercentage =
                entries.count { it.isSeen }.toDouble() / pokedex.pokemonCount * 100
            Text(text = "${seenPercentage.toInt()}% Completed")
        }
    }
}

@Composable
private fun PokedexEntry(
    species: String,
    spriteSource: SpriteSource,
    id: Int,
    isSeen: Boolean,
    isOwned: Boolean,
    onValueChange: () -> Unit
) {
    val emphasisedColor = EmphasisAmbient.current.run { if (!isSeen) disabled else medium }
        .applyEmphasis(MaterialTheme.colors.onSurface)
    Row(
        verticalGravity = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .preferredHeight(56.dp)
            .clickable(onClick = onValueChange)
    ) {
        Spacer(modifier = Modifier.preferredWidth(16.dp))
        if (!isSeen) {
            Image(
                modifier = PokemonSpriteSize,
                colorFilter = ColorFilter.tint(emphasisedColor),
                asset = imageResource(R.drawable.pokeball_s)
            )
        } else {
            CoilImage(data = spriteSource.value, modifier = PokemonSpriteSize)
        }
        Text(
            text = "#$id",
            modifier = Modifier.padding(start = 24.dp, end = 24.dp),
            color = emphasisedColor,
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium)
        )
        Text(
            text = species,
            color = emphasisedColor,
            style = MaterialTheme.typography.body1
        )
        if (isOwned)
            Label(modifier = Modifier.padding(start = 24.dp), text = "Owned")
    }
}
