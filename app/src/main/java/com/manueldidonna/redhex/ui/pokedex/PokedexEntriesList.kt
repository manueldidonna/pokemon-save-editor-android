package com.manueldidonna.redhex.ui.pokedex

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manueldidonna.redhex.common.ItemSpriteModifier
import com.manueldidonna.redhex.common.PokemonSpriteModifier
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun PokedexEntriesList(
    modifier: Modifier = Modifier,
    pokedexEntries: List<PokedexEntry>,
    pokemonSeenCount: Int,
    onEntryChange: (PokedexEntry) -> Unit,
) {
    if (pokedexEntries.isEmpty()) return
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        item {
            Text(
                text = "$pokemonSeenCount pokemon seen out of ${pokedexEntries.size}",
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.body2,
                color = AmbientContentColor.current.copy(alpha = ContentAlpha.medium),
                modifier = Modifier.padding(bottom = 16.dp, start = 24.dp, top = 8.dp),
            )
        }
        items(pokedexEntries) {
            PokedexEntry(
                speciesName = it.speciesName,
                spriteSource = it.spriteSource,
                isSeen = it.isSeen,
                isOwned = it.isOwned,
                onClick = { onEntryChange(it.mutateOnClick()) }
            )
        }
    }
}

private fun PokedexEntry.mutateOnClick(): PokedexEntry {
    return when {
        isOwned -> copy(isSeen = false, isOwned = false)
        isSeen -> copy(isOwned = true)
        else -> copy(isSeen = true)
    }
}

@Composable
private fun PokedexEntry(
    speciesName: String,
    spriteSource: Any,
    isSeen: Boolean,
    isOwned: Boolean,
    onClick: () -> Unit,
) {
    val color =
        if (isSeen) AmbientContentColor.current
        else AmbientContentColor.current.copy(alpha = ContentAlpha.disabled)
    val colorFilter = remember(isSeen) { if (isSeen) null else ColorFilter.tint(color) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(56.dp)
            .clickable(
                onClick = onClick,
                indication = rememberRipple(color = MaterialTheme.colors.primary)
            )
    ) {
        CoilImage(
            data = spriteSource,
            colorFilter = colorFilter,
            modifier = PokemonSpriteModifier,
        )
        Text(
            text = speciesName,
            color = color,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(1f)
        )
        if (isOwned) {
            Icon(Icons.TwoTone.CheckCircle, tint = MaterialTheme.colors.primary)
        }
        Spacer(Modifier.width(16.dp))
    }
}