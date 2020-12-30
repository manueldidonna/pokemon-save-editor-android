package com.manueldidonna.redhex.ui.bag

import androidx.compose.animation.animate
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.twotone.MoreVert
import androidx.compose.material.icons.twotone.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.emptyContent
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.AmbientPokemonTextResources
import com.manueldidonna.redhex.DefaultIcons
import com.manueldidonna.redhex.PokemonEditorTheme
import com.manueldidonna.redhex.common.AppBarElevation
import com.manueldidonna.redhex.common.AppBarHeight
import com.manueldidonna.redhex.common.AppBarIconButton
import java.util.*

@Composable
fun BagAppBar(
    modifier: Modifier = Modifier,
    inventoryTypes: List<Inventory.Type>,
    selectedType: Inventory.Type,
    onTypeChange: (Inventory.Type) -> Unit,
    filterItems: (query: String) -> Unit,
) {
    Surface(elevation = AppBarElevation, modifier = modifier) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(AppBarHeight).padding(horizontal = 4.dp),
            ) {
                Icon(
                    DefaultIcons.Search,
                    modifier = Modifier.padding(start = 12.dp, end = 32.dp)
                )
                Providers(AmbientContentAlpha provides ContentAlpha.disabled) {
                    Text(
                        "Search...",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.subtitle1
                    )
                }
                AppBarIconButton(icon = DefaultIcons.MoreVert, onClick = { /*TODO*/ })
            }
            Divider()
            InventoryItemsTabRow(
                inventoryTypes = inventoryTypes,
                selectedType = selectedType,
                onTypeChange = onTypeChange
            )
        }
    }
}

@Composable
private fun InventoryItemsTabRow(
    inventoryTypes: List<Inventory.Type>,
    selectedType: Inventory.Type,
    onTypeChange: (Inventory.Type) -> Unit,
) {
    val pokemonTextResources = AmbientPokemonTextResources.current.items
    val inventoryTypeNames = remember {
        inventoryTypes.map { pokemonTextResources.getTypeName(it).toUpperCase(Locale.ROOT) }
    }
    val selectedTypeIndex = inventoryTypes.indexOf(selectedType)
    ScrollableTabRow(
        selectedTabIndex = selectedTypeIndex,
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.primary,
        divider = emptyContent(),
        indicator = { SmoothTabIndicator(it[selectedTypeIndex]) }
    ) {
        inventoryTypes.forEachIndexed { index, type ->
            Tab(
                selected = index == selectedTypeIndex,
                onClick = { onTypeChange(type) },
                text = { Text(inventoryTypeNames[index]) }
            )
        }
    }
}

private val TabIndicatorAnimSpec = tween<Dp>(durationMillis = 250, easing = FastOutSlowInEasing)

@Composable
private fun SmoothTabIndicator(currentTabPosition: TabPosition) {
    val indicatorWidth = animate(currentTabPosition.width - 8.dp, TabIndicatorAnimSpec)
    val indicatorOffset = animate(currentTabPosition.left + 4.dp, TabIndicatorAnimSpec)
    Box(
        Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.BottomStart)
            .offset(x = indicatorOffset)
            .preferredWidth(indicatorWidth)
            .preferredHeight(3.dp)
            .clip(RoundedCornerShape(topLeft = 12.dp, topRight = 12.dp))
            .background(color = MaterialTheme.colors.primary)

    )
}

@Composable
@Preview
private fun PreviewSearch() {
    PokemonEditorTheme {
        Surface {
            Providers(AmbientPokemonTextResources provides PokemonTextResources.English) {
                BagAppBar(
                    inventoryTypes = listOf(Inventory.Type.General, Inventory.Type.Balls),
                    selectedType = Inventory.Type.General,
                    onTypeChange = { /*TODO*/ },
                    filterItems = { /*TODO*/ }
                )
            }
        }
    }
}