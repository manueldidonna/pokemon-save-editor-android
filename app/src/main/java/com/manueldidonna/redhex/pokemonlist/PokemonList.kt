package com.manueldidonna.redhex.pokemonlist

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.zIndex
import androidx.ui.foundation.*
import androidx.ui.graphics.ColorFilter
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.ChevronLeft
import androidx.ui.material.icons.twotone.ChevronRight
import androidx.ui.res.imageResource
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.*
import com.manueldidonna.redhex.R
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpriteSize
import com.manueldidonna.redhex.common.SpriteSource
import com.manueldidonna.redhex.common.SpritesRetrieverAmbient
import com.manueldidonna.redhex.common.ui.ToolbarHeight
import com.manueldidonna.redhex.common.ui.TranslucentToolbar
import dev.chrisbanes.accompanist.coil.CoilImage

private data class PokemonPreview(
    val nickname: String,
    val label: String,
    val sprite: SpriteSource
)

@Composable
fun PokemonList(
    modifier: Modifier = Modifier,
    collection: StorageCollection,
    showPokemonDetails: (Pokemon.Position) -> Unit
) {
    val pokemonResources = PokemonResourcesAmbient.current.natures
    val spritesRetriever = SpritesRetrieverAmbient.current

    var currentIndex: Int by state { collection.currentIndex }

    val storage: MutableStorage by stateFor(currentIndex) {
        collection.getMutableStorage(currentIndex)
    }

    val pokemonPreviews by stateFor(storage.index) {
        List(storage.capacity) { i ->
            storage.getPokemon(i).run {
                if (isEmpty) return@run null
                return@run PokemonPreview(
                    nickname = nickname,
                    label = "${pokemonResources.getNatureById(natureId)} - Lv.$level",
                    sprite = spritesRetriever.getPokemonSprite(speciesId)
                )
            }
        }
    }

    Stack(modifier = modifier) {
        Toolbar(
            modifier = Modifier.height(ToolbarHeight).zIndex(8f).gravity(Alignment.TopCenter),
            title = storage.name,
            onBack = { currentIndex = collection.decreaseIndex() },
            onForward = { currentIndex = collection.increaseIndex() }
        )
        VerticalScroller {
            Spacer(modifier = Modifier.preferredHeight(ToolbarHeight))
            ShowPokemonPreviews(
                previews = pokemonPreviews,
                onSlotSelection = { slot ->
                    showPokemonDetails(Pokemon.Position(currentIndex, slot))
                }
            )
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
    previews: List<PokemonPreview?>,
    onSlotSelection: (slot: Int) -> Unit
) {
    val activeTextColor = MaterialTheme.colors.onSurface
    val disabledTextColor = EmphasisAmbient.current.disabled.applyEmphasis(activeTextColor)
    previews.forEachIndexed { index, pk ->
        ListItem(
            text = {
                if (pk == null) {
                    Text(text = "Empty Slot", color = disabledTextColor)
                } else {
                    Text(text = pk.nickname, color = activeTextColor)
                }
            },
            icon = {
                Box(gravity = ContentGravity.Center, modifier = Modifier.size(40.dp)) {
                    PokemonSprite(source = pk?.sprite?.value)
                }
            },
            onClick = { onSlotSelection(index) },
            secondaryText = PokemonLabel(pk)
        )
        Divider()
    }
}

@Composable
private fun PokemonLabel(pk: PokemonPreview?): @Composable (() -> Unit)? {
    if (pk == null) return null
    return {
        Text(text = pk.label)
    }
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
