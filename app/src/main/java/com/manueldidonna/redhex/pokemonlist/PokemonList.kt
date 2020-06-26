package com.manueldidonna.redhex.pokemonlist

import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.stateFor
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
import androidx.ui.savedinstancestate.Saver
import androidx.ui.savedinstancestate.savedInstanceState
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.core.StorageIndex
import com.manueldidonna.pk.core.isParty
import com.manueldidonna.redhex.R
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpriteSize
import com.manueldidonna.redhex.common.SpriteSource
import com.manueldidonna.redhex.common.SpritesRetrieverAmbient
import com.manueldidonna.redhex.common.ui.ToolbarHeight
import com.manueldidonna.redhex.common.ui.TranslucentToolbar
import dev.chrisbanes.accompanist.coil.CoilImage

private val StorageIndexSaver = Saver<StorageIndex, Int>(
    save = { it.value },
    restore = {
        if (it == StorageIndex.Party.value)
            StorageIndex.Party
        else
            StorageIndex.Box(it, isCurrentBox = true)
    }
)

private data class PokemonPreview(
    val nickname: String,
    val label: String,
    val sprite: SpriteSource
)

@Composable
fun PokemonList(
    modifier: Modifier = Modifier,
    saveData: SaveData,
    showPokemonDetails: (Pokemon.Position) -> Unit
) {
    val pokemonResources = PokemonResourcesAmbient.current.natures
    val spritesRetriever = SpritesRetrieverAmbient.current

    var storageIndex: StorageIndex by savedInstanceState(saver = StorageIndexSaver) {
        StorageIndex.Box(saveData.currentBoxIndex, isCurrentBox = true)
    }

    val storage by stateFor(storageIndex.value) {
        saveData.getMutableStorage(storageIndex)
    }

    val pokemonPreviews by stateFor(storage.index.value) {
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
            onBack = {
                storageIndex = storageIndex.nextIndex(saveData, increase = false)
            },
            onForward = {
                storageIndex = storageIndex.nextIndex(saveData, increase = true)
            }
        )
        VerticalScroller {
            Spacer(modifier = Modifier.preferredHeight(ToolbarHeight))
            ShowPokemonPreviews(
                previews = pokemonPreviews,
                onSlotSelection = { slot ->
                    showPokemonDetails(Pokemon.Position(storageIndex, slot))
                }
            )
        }
    }
}

private fun StorageIndex.nextIndex(saveData: SaveData, increase: Boolean): StorageIndex {
    val newIndexValue = value + (if (increase) 1 else -1)
    val maxIndexValue = saveData.boxCount - 1
    val nextIndex = when {
        isParty -> StorageIndex.Box(if (increase) 0 else maxIndexValue, isCurrentBox = true)
        newIndexValue in 0..maxIndexValue -> StorageIndex.Box(newIndexValue, isCurrentBox = true)
        else -> StorageIndex.Party
    }
    if (!nextIndex.isParty) {
        saveData.currentBoxIndex = nextIndex.value
    }
    return nextIndex
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
