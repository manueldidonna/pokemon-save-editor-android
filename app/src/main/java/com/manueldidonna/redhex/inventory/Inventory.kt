package com.manueldidonna.redhex.inventory

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Bag
import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.isFull
import com.manueldidonna.pk.core.stackItem
import com.manueldidonna.redhex.common.*
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun Inventory(modifier: Modifier, bag: Bag) {
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

    Stack(modifier = modifier.fillMaxSize()) {
        Column {
            InventoryTypes(types = inventoryTypes, setType)
            LazyColumnFor(items = items) { item ->
                InventoryItem(item = item, selectItem)
                Divider()
            }
        }
        if (!inventory.value.isFull)
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
        InventoryEditor(
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
    FloatingActionButton(
        modifier = modifier,
        icon = { Icon(asset = Icons.TwoTone.Add) },
        onClick = onClick
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
        selectedTabIndex = selectedIndex,
        modifier = Modifier.fillMaxWidth(),
    ) {
        types.forEachIndexed { index, type ->
            Tab(
                modifier = Modifier.preferredHeight(48.dp),
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
    ListItem(
        text = { Text(text = item.name) },
        secondaryText = { Text(text = "Qt. ${item.quantity}") },
        modifier = Modifier.clickable(onClick = { onClick(item) }),
        icon = {
            Box(gravity = ContentGravity.Center, modifier = Modifier.size(40.dp)) {
                CoilImage(data = item.spriteSource.value, modifier = ItemSpriteSize)
            }
        }
    )
}
