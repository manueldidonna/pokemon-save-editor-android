package com.manueldidonna.redhex.inventory

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.zIndex
import androidx.ui.foundation.AdapterList
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.layout.*
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Tab
import androidx.ui.material.TabRow
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.core.getAllItems
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.ui.Counter
import com.manueldidonna.redhex.common.ui.DialogItem
import com.manueldidonna.redhex.common.ui.DialogMenu
import com.manueldidonna.redhex.translucentSurfaceColor

@Composable
fun InventoryScreen(modifier: Modifier, saveData: SaveData) {
    val resources = PokemonResourcesAmbient.current.items
    val itemNames = remember { resources.getAllItems(saveData.version) }

    val inventories: List<Inventory> = remember {
        saveData.supportedInventoryTypes.map { type -> saveData.getInventory(type)!! }
    }

    var selectedInventory by state { inventories.first() }

    var items: List<Inventory.Item> by state { inventories.first().getAllItems() }

    var selectedIndex by state { -1 }

    Stack(modifier = modifier) {
        InventoryTabs(inventories) { inventory ->
            items = inventory.getAllItems()
            selectedInventory = inventory
        }
        Column {
            Spacer(modifier = Modifier.height(48.dp))
            if (items.isNotEmpty()) {
                ItemsList(
                    items = items,
                    names = itemNames,
                    maxAllowedQuantity = selectedInventory.maxAllowedQuantity,
                    onItemChange = { newItem ->
                        selectedInventory.setItem(newItem)
                        items = selectedInventory.getAllItems()
                    },
                    onItemClick = { index -> selectedIndex = index }
                )
            }
        }
    }

    if (selectedIndex >= 0) {
        ItemsChooser(
            itemNames = itemNames,
            itemIds = selectedInventory.supportedItemIds,
            dismiss = { selectedIndex = -1 },
            onItemSelected = { itemId ->
                val item = Inventory.Item(index = selectedIndex, id = itemId, quantity = 1)
                selectedInventory.setItem(item)
                items = selectedInventory.getAllItems()
            }
        )
    }
}

@Composable
private fun InventoryTabs(inventories: List<Inventory>, onInventoryChange: (Inventory) -> Unit) {
    val resources = PokemonResourcesAmbient.current.items
    var selectedIndex by state { 0 }
    TabRow(
        backgroundColor = translucentSurfaceColor(),
        items = inventories,
        modifier = Modifier.zIndex(8f).fillMaxWidth(),
        selectedIndex = selectedIndex,
        scrollable = false
    ) { index, inventory ->
        Tab(
            modifier = Modifier.preferredHeight(48.dp),
            text = { Text(text = resources.getTypeName(inventory.type)) },
            selected = index == selectedIndex,
            onSelected = {
                onInventoryChange(inventory)
                selectedIndex = index
            }
        )
    }
}

@Composable
private fun ItemsList(
    maxAllowedQuantity: Int,
    items: List<Inventory.Item>,
    names: List<String>,
    onItemChange: (Inventory.Item) -> Unit,
    onItemClick: (index: Int) -> Unit
) {
    AdapterList(items) { item ->
        ItemRow(
            name = names[item.id],
            quantity = item.quantity,
            maxAllowedQuantity = maxAllowedQuantity,
            onQuantityChange = { newQuantity ->
                onItemChange(item.copy(quantity = newQuantity))
            },
            onItemClick = { onItemClick(item.index) }
        )
    }
}

@Composable
private fun ItemRow(
    name: String,
    quantity: Int,
    maxAllowedQuantity: Int,
    onQuantityChange: (Int) -> Unit,
    onItemClick: () -> Unit
) {
    Row(
        verticalGravity = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onItemClick)
            .padding(horizontal = 16.dp)
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Text(text = name, style = MaterialTheme.typography.body1, modifier = Modifier.weight(1f))
        if (quantity > 0)
            Counter(
                modifier = Modifier.width(140.dp),
                value = quantity,
                onValueChanged = onQuantityChange,
                enableIncrease = quantity < maxAllowedQuantity,
                enableDecrease = quantity > 1
            )
    }
}

@Composable
private inline fun ItemsChooser(
    itemNames: List<String>,
    itemIds: List<Int>,
    noinline dismiss: () -> Unit,
    crossinline onItemSelected: (id: Int) -> Unit
) {
    DialogMenu(dismiss = dismiss) {
        AdapterList(data = itemIds) {
            DialogItem(text = itemNames[it], onClick = { onItemSelected(it) })
        }
    }
}
