package com.manueldidonna.redhex.pokemonlist

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.graphics.ColorFilter
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.ChevronLeft
import androidx.ui.material.icons.twotone.ChevronRight
import androidx.ui.res.imageResource
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.R
import com.manueldidonna.redhex.common.*
import com.manueldidonna.redhex.common.ui.ToolbarHeight
import com.manueldidonna.redhex.common.ui.TranslucentToolbar
import dev.chrisbanes.accompanist.coil.CoilImage

private data class PokemonPreview(
    val isEmpty: Boolean,
    val slot: Int,
    val nickname: String,
    val label: String,
    val source: SpriteSource
)

@Composable
fun PokemonList(
    modifier: Modifier = Modifier,
    collection: StorageCollection,
    showPokemonDetails: (Pokemon.Position) -> Unit
) {
    val resources = PokemonResourcesAmbient.current.natures
    val spritesRetriever = SpritesRetrieverAmbient.current

    var currentIndex: Int by state { collection.currentIndex }

    val storage: MutableStorage by stateFor(currentIndex) {
        collection.getMutableStorage(currentIndex)
    }

    val pokemonPreviews: List<PokemonPreview> by stateFor(storage.index) {
        storage.getPokemonPreviews(resources, spritesRetriever)
    }

    Column(modifier = modifier) {
        Toolbar(
            modifier = Modifier.height(ToolbarHeight),
            title = storage.name,
            onBack = { currentIndex = collection.decreaseIndex() },
            onForward = { currentIndex = collection.increaseIndex() }
        )
        ShowPokemonPreviews(
            previews = pokemonPreviews,
            onSlotSelection = { slot ->
                showPokemonDetails(Pokemon.Position(currentIndex, slot))
            }
        )
    }
}

private fun Storage.getPokemonPreviews(
    resources: PokemonTextResources.Natures,
    spritesRetriever: SpritesRetriever
): List<PokemonPreview> {
    return List(capacity) { i ->
        getPokemon(i).run {
            if (isEmpty) {
                PokemonPreview(
                    nickname = "Empty Slot",
                    label = "",
                    source = SpriteSource.Invalid,
                    slot = position.slot,
                    isEmpty = true
                )
            } else {
                PokemonPreview(
                    isEmpty = false,
                    slot = position.slot,
                    nickname = nickname,
                    label = "${resources.getNatureById(natureId)} - Lv.$level",
                    source = spritesRetriever.getPokemonSprite(speciesId)
                )
            }
        }
    }
}

@Composable
private fun Toolbar(
    modifier: Modifier = Modifier,
    title: String,
    onBack: () -> Unit,
    onForward: () -> Unit
) {
    TranslucentToolbar(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        IconButton(onClick = onBack) {
            Icon(Icons.TwoTone.ChevronLeft, tint = MaterialTheme.colors.onSurface)
        }
        Text(
            modifier = Modifier
                .preferredWidthIn(minWidth = 150.dp)
                .wrapContentWidth(Alignment.CenterHorizontally),
            text = title
        )
        IconButton(onClick = onForward) {
            Icon(Icons.TwoTone.ChevronRight, tint = MaterialTheme.colors.onSurface)
        }
    }
}

@Composable
private fun ShowPokemonPreviews(
    previews: List<PokemonPreview>,
    onSlotSelection: (slot: Int) -> Unit
) {
    val activeTextColor = MaterialTheme.colors.onSurface
    val disabledTextColor = EmphasisAmbient.current.disabled.applyEmphasis(activeTextColor)
    LazyColumnItems(items = previews) { pk ->
        ListItem(
            text = {
                Text(
                    text = pk.nickname,
                    color = if (pk.isEmpty) disabledTextColor else activeTextColor
                )
            },
            icon = {
                Box(gravity = ContentGravity.Center, modifier = Modifier.size(40.dp)) {
                    PokemonSprite(source = pk.source)
                }
            },
            onClick = { onSlotSelection(pk.slot) },
            secondaryText = PokemonLabel(pk.label)
        )
        Divider()
    }
}

@Composable
private fun PokemonLabel(text: String): @Composable (() -> Unit)? {
    if (text.isEmpty()) return null
    return {
        Text(text = text)
    }
}

@Composable
private fun PokemonSprite(source: SpriteSource) {
    // placeholder for empty slot
    if (source.value == SpriteSource.Invalid.value) {
        val emphasis = EmphasisAmbient.current.disabled
        Image(
            modifier = PokemonSpriteSize,
            colorFilter = ColorFilter.tint(emphasis.applyEmphasis(MaterialTheme.colors.onSurface)),
            asset = imageResource(R.drawable.pokeball_s)
        )
    } else {
        CoilImage(data = source.value, modifier = PokemonSpriteSize)
    }
}
