package com.manueldidonna.redhex.home

import android.util.Log
import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.zIndex
import androidx.ui.foundation.*
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.ChevronLeft
import androidx.ui.material.icons.twotone.ChevronRight
import androidx.ui.material.ripple.RippleIndication
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.PokemonSpritesRetrieverAmbient
import com.manueldidonna.redhex.common.pokemon.PokemonSpritesRetriever
import com.manueldidonna.redhex.common.ui.ToolbarHeight
import com.manueldidonna.redhex.common.ui.TranslucentToolbar
import com.manueldidonna.redhex.home.HomeAction.*
import java.io.File

private sealed class HomeAction {
    object IncreaseBoxIndex : HomeAction()
    object DecreaseBoxIndex : HomeAction()
    data class DeleteSlot(val slot: Int) : HomeAction()
}

interface HomeEvents {
    fun showPokemonDetails(position: Pokemon.Position)
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier, saveData: SaveData, listener: HomeEvents) {
    val pokemonResources = PokemonResourcesAmbient.current
    val spritesRetriever = PokemonSpritesRetrieverAmbient.current

    var currentStorageIndex: StorageIndex by state {
        StorageIndex.Box(saveData.currentBoxIndex, isCurrentBox = true)
    }

    val currentStorage by stateFor(currentStorageIndex.value) {
        saveData.getMutableStorage(currentStorageIndex)
    }

    val pokemonPreviews = stateFor(currentStorage) {
        getPokemonPreviews(currentStorage, pokemonResources, spritesRetriever)
    }

    fun executeAction(action: HomeAction) {
        when (action) {
            IncreaseBoxIndex, DecreaseBoxIndex -> {
                val sumValue = if (action == IncreaseBoxIndex) 1 else -1
                val wasParty = currentStorageIndex.isParty
                val newIndex = currentStorageIndex.value + sumValue
                val maxBoxIndex = saveData.boxCounts - 1
                currentStorageIndex = when {
                    newIndex in 0..maxBoxIndex -> {
                        saveData.currentBoxIndex = newIndex
                        StorageIndex.Box(newIndex, isCurrentBox = true)
                    }
                    wasParty -> {
                        StorageIndex.Box(if (sumValue == 1) 0 else maxBoxIndex, isCurrentBox = true)
                    }
                    else -> StorageIndex.Party
                }
            }
            is DeleteSlot -> {
                Log.d("delete slot", "slot: ${action.slot}")
                currentStorage.deletePokemon(action.slot)
                pokemonPreviews.value =
                    getPokemonPreviews(currentStorage, pokemonResources, spritesRetriever)
            }
        }
    }

    Stack(modifier = modifier) {
        VerticalScroller {
            Column {
                Spacer(modifier = Modifier.preferredHeight(56.dp))
                PokemonList(
                    pokemon = pokemonPreviews.value,
                    deletePokemon = { slot -> executeAction(DeleteSlot(slot)) },
                    onSelection = { slot ->
                        listener.showPokemonDetails(
                            Pokemon.Position(currentStorageIndex, slot)
                        )
                    }
                )
                Spacer(modifier = Modifier.preferredHeight(16.dp))
            }
        }
        HomeToolbar(
            // TODO: there is a bug with zIndex. Check again in dev-12
            // It doesn't receive cliks if positioned before the views that it overlaps
            modifier = Modifier.preferredHeight(ToolbarHeight).zIndex(8f).gravity(Alignment.TopCenter),
            title = currentStorage.name,
            onBack = { executeAction(DecreaseBoxIndex) },
            onForward = { executeAction(IncreaseBoxIndex) }
        )
    }
}

private fun getPokemonPreviews(
    storage: Storage,
    resources: PokemonTextResources,
    spritesRetriever: PokemonSpritesRetriever
): List<PokemonPreview?> {
    return List(storage.pokemonCounts) { i ->
        storage.getPokemon(i).run {
            if (isEmpty) null else {
                PokemonPreview(
                    nickname = nickname,
                    labels = listOf("L. $level", resources.natures.getNatureById(natureId)),
                    sprite = File(spritesRetriever.getSpritesPathFromId(speciesId))
                )
            }
        }
    }
}

@Composable
private fun HomeToolbar(
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
private fun PokemonList(
    pokemon: List<PokemonPreview?>,
    onSelection: (slot: Int) -> Unit,
    deletePokemon: (slot: Int) -> Unit
) {
    var selectedIndex by state { -1 }
    pokemon.forEachIndexed { index, pk ->
        ContextualMenu(
            expanded = selectedIndex == index,
            onDismissRequest = { selectedIndex = -1 },
            deletePokemon = { deletePokemon(selectedIndex) }
        ) {
            PokemonCard(
                preview = pk,
                clickable = Modifier.clickable(
                    onClick = { onSelection(index) },
                    onLongClick = { selectedIndex = index }
                )
            )
        }
    }
}

@Composable
private fun ContextualMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    deletePokemon: () -> Unit,
    anchorTo: @Composable() () -> Unit
) {
    fun dismissAfterClick(click: () -> Unit): () -> Unit = {
        click()
        onDismissRequest()
    }
    DropdownMenu(
        toggle = anchorTo,
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        FixedDropDownMenuItem(onClick = dismissAfterClick(deletePokemon)) {
            Text("Delete Pokemon")
        }
    }
}


@Composable //TODO: remove when a bug in ui-material will be fixed
private fun FixedDropDownMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable() () -> Unit
) {
    Box(
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick, indication = RippleIndication())
            .fillMaxWidth()
            // Preferred min and max width used during the intrinsic measurement.
            .preferredSizeIn(
                minWidth = DropdownMenuItemDefaultMinWidth,
                maxWidth = DropdownMenuItemDefaultMaxWidth,
                minHeight = DropdownMenuItemDefaultMinHeight
            )
            .padding(horizontal = DropdownMenuHorizontalPadding),
        gravity = ContentGravity.CenterStart
    ) {
        // TODO(popam, b/156912039): update emphasis if the menu item is disabled
        val typography = MaterialTheme.typography
        val emphasisLevels = EmphasisAmbient.current
        ProvideTextStyle(typography.subtitle1) {
            ProvideEmphasis(
                if (enabled) emphasisLevels.high else emphasisLevels.disabled,
                content
            )
        }
    }
}

private val DropdownMenuHorizontalPadding = 16.dp
private val DropdownMenuItemDefaultMinWidth = 112.dp
private val DropdownMenuItemDefaultMaxWidth = 280.dp
private val DropdownMenuItemDefaultMinHeight = 48.dp
