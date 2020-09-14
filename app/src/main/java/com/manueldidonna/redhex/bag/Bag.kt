package com.manueldidonna.redhex.bag

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.manueldidonna.pk.core.Bag
import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.isFull
import com.manueldidonna.pk.core.stackItem
import com.manueldidonna.redhex.common.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun Bag(bag: Bag) {
    val inventoryTypes = remember { bag.inventoryTypes.toList() }

    val (type, setType) = rememberMutableState { inventoryTypes.first() }

    val inventory = getInventory(bag = bag, type = type)

    val items by remember(type) { inventory.value.items }

    // fetch all the items when the inventory change
    launchInComposition(inventory) {
        val currentInventory = inventory.value
        withContext(Dispatchers.IO) {
            currentInventory.fetchAllItems()
        }
    }

    val (selectedItem, selectItem) = rememberMutableState { InventoryItem.Invalid }

    Stack(modifier = Modifier.fillMaxSize()) {
        val isInventoryFull = inventory.value.isFull
        InventoryTypes(types = inventoryTypes, setType)
        LazyColumnForIndexed(
            items = items,
            modifier = Modifier.fillMaxSize()
        ) { index, item ->
            if (index == 0) {
                Spacer(Modifier.height(48.dp))
            }
            InventoryItem(item = item, selectItem)
            if (index == items.lastIndex && !isInventoryFull) {
                Spacer(Modifier.height(72.dp))
            } else {
                Divider()
            }
        }
        if (!isInventoryFull)
            InsertItemButton(Modifier.padding(16.dp).gravity(Alignment.BottomEnd)) {
                selectItem(InventoryItem.empty(index = inventory.value.size))
            }
    }

    if (selectedItem != InventoryItem.Invalid)
        InventoryEditorDialog(
            onDismissRequest = { selectItem(InventoryItem.Invalid) },
            inventory = inventory.value,
            item = selectedItem
        )
}

@Composable
private fun InventoryEditorDialog(
    onDismissRequest: () -> Unit,
    inventory: ObservableInventory,
    item: InventoryItem,
) {
    // TODO: use a bottom sheet
    val scope = rememberCoroutineScope()
    ThemedDialog(onDismissRequest = onDismissRequest) {
        InventoryItemEditor(
            item = item,
            itemIds = inventory.supportedItemIds,
            maxAllowedQuantity = inventory.maxQuantity,
            onItemChange = { item ->
                val areItemsDifferent = inventory.selectItem(item.index) { _, id, quantity ->
                    item.id != id || item.quantity != quantity
                }
                if (areItemsDifferent) {
                    scope.launch(Dispatchers.IO) {
                        inventory.stackItem(item)
                    }
                }
                onDismissRequest()
            }
        )
    }
}

@Composable
private fun InsertItemButton(modifier: Modifier, onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        text = { Text(text = "INSERT ITEM") },
        icon = { Icon(asset = Icons.TwoTone.Add) },
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
@Stable
private fun getInventory(bag: Bag, type: Inventory.Type): State<ObservableInventory> {
    val resources = PokemonResourcesAmbient.current.items
    val spritesRetriever = SpritesRetrieverAmbient.current
    return rememberMutableStateFor(type) {
        ObservableInventory(bag[type], resources, spritesRetriever)
    }
}

@Composable
private fun InventoryTypes(types: List<Inventory.Type>, onTypeChange: (Inventory.Type) -> Unit) {
    val resources = PokemonResourcesAmbient.current.items
    var selectedIndex by rememberMutableState { 0 }
    ScrollableTabRow(
        contentColor = MaterialTheme.colors.onSurface,
        backgroundColor = TranslucentSurfaceColor(),
        edgePadding = 0.dp,
        selectedTabIndex = selectedIndex,
        modifier = Modifier.fillMaxWidth().height(AppBarHeight).zIndex(8f),
    ) {
        types.forEachIndexed { index, type ->
            Tab(
                modifier = Modifier.preferredHeight(AppBarHeight),
                content = { Text(text = resources.getTypeName(type)) },
                selected = index == selectedIndex,
                onClick = {
                    onTypeChange(type)
                    selectedIndex = index
                }
            )
        }
    }
}

@Composable
private fun InventoryItem(item: InventoryItem, onClick: (item: InventoryItem) -> Unit) {
    ListItemWithSprite(
        primaryText = item.name,
        secondaryText = "Qt. ${item.quantity}",
        spriteSource = item.spriteSource,
        modifier = Modifier.clickable(onClick = { onClick(item) })
    )
}
