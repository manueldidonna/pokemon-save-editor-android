package com.manueldidonna.redhex.inventory

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.zIndex
import androidx.ui.foundation.AdapterList
import androidx.ui.foundation.Border
import androidx.ui.foundation.Text
import androidx.ui.foundation.clickable
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.core.getAllItems
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.ui.*
import com.manueldidonna.redhex.translucentSurfaceColor

@Composable
fun InventoryScreen(modifier: Modifier, saveData: SaveData) {
    val resources = PokemonResourcesAmbient.current.items

    val inventories: List<Inventory> = remember {
        saveData.supportedInventoryTypes.map { type -> saveData.getInventory(type)!! }
    }

    var selectedInventory by state { inventories.first() }

    var items: List<Inventory.Item> by state { inventories.first().getAllItems() }

    var selectedItemIndex by state { -1 }

    Column(modifier) {
        InventoryTabs(inventories) { inventory ->
            items = inventory.getAllItems()
            selectedInventory = inventory
        }
        ItemsList(
            items = items,
            names = resources.getAllItems(saveData.version),
            maxAllowedQuantity = selectedInventory.maxAllowedQuantity,
            capacity = selectedInventory.capacity,
            onItemChange = { newItem ->
                selectedInventory.setItem(newItem)
                items = selectedInventory.getAllItems()
            },
            onItemClick = { index -> selectedItemIndex = index }
        )
    }

    if (selectedItemIndex >= 0) {
        ItemsChooser(
            itemNames = resources.getAllItems(saveData.version),
            itemIds = selectedInventory.supportedItemIds,
            dismiss = { selectedItemIndex = -1 },
            onItemSelected = { itemId ->
                val item = Inventory.Item(index = selectedItemIndex, id = itemId, quantity = 1)
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
        scrollable = false // TODO: this should be scrollable
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
    capacity: Int,
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
        if (item.index == items.lastIndex) {
            if (item.index < capacity - 1)
                AddItemRow(onClick = { onItemClick(item.index + 1) })
        } else {
            Divider()
        }
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
            .padding(horizontal = 16.dp)
            .height(56.dp)
            .clickable(onClick = onItemClick)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colors.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        Stepper(
            value = quantity,
            onValueChanged = onQuantityChange,
            enableIncrease = quantity < maxAllowedQuantity,
            enableDecrease = quantity > 1
        )
    }
}

@Composable
private fun AddItemRow(onClick: () -> Unit) {
    OutlinedButton(
        border = Border(size = 1.dp, color = MaterialTheme.colors.primary),
        contentColor = MaterialTheme.colors.primary,
        onClick = onClick,
        modifier = Modifier.padding(24.dp).fillMaxWidth()
    ) {
        Text("ADD AN ITEM")
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

@Preview
@Composable
private fun PreviewItemRow() {
    PreviewScreen(colors = LightColors) {
        ItemRow(
            name = "Masterball",
            quantity = 97,
            maxAllowedQuantity = 99,
            onQuantityChange = {},
            onItemClick = {}
        )
    }
}
