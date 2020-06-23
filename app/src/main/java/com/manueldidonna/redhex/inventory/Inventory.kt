package com.manueldidonna.redhex.inventory

import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.zIndex
import androidx.ui.foundation.AdapterList
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.Add
import androidx.ui.text.font.FontWeight
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.core.getAllItems
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.ui.LightColors
import com.manueldidonna.redhex.common.ui.PreviewScreen
import com.manueldidonna.redhex.common.ui.ThemedDialog
import com.manueldidonna.redhex.common.ui.translucentSurfaceColor
import kotlin.math.roundToInt

private val NullItem = Inventory.Item.empty(index = -1)

@Composable
fun Inventory(modifier: Modifier, saveData: SaveData) {
    val resources = PokemonResourcesAmbient.current.items

    var inventory: Inventory by state {
        saveData.getInventory(saveData.supportedInventoryTypes.first())
    }

    var items: List<Inventory.Item> by state {
        inventory.getAllItems()
    }

    var selectedItem: Inventory.Item by state { NullItem }

    Stack(modifier = modifier) {
        Column {
            InventoryTypes(types = saveData.supportedInventoryTypes) { type ->
                inventory = saveData.getInventory(type)
                items = inventory.getAllItems()
            }
            ItemsList(
                items = items,
                names = resources.getAllItems(saveData.version),
                onItemClick = { item -> selectedItem = item }
            )
        }
        if (inventory.size < inventory.capacity)
            FloatingActionButton(
                modifier = Modifier.padding(16.dp).gravity(Alignment.BottomEnd),
                onClick = { selectedItem = Inventory.Item.empty(index = inventory.size) },
                icon = { Icon(asset = Icons.TwoTone.Add) }
            )
    }


    if (selectedItem != NullItem) {
        val onCloseRequest = { selectedItem = NullItem }
        // TODO: use a bottom sheet
        ThemedDialog(onCloseRequest = onCloseRequest) {
            ItemEditor(
                item = selectedItem,
                itemNames = resources.getAllItems(saveData.version),
                itemIds = inventory.supportedItemIds,
                maxAllowedQuantity = inventory.maxAllowedQuantity,
                onItemChange = { item ->
                    if (inventory.getItem(item.index) != item) {
                        inventory.setItem(item)
                        items = inventory.getAllItems()
                    }
                },
                onCloseRequest = onCloseRequest
            )
        }
    }
}

@Composable
private fun ItemEditor(
    item: Inventory.Item,
    maxAllowedQuantity: Int,
    itemNames: List<String>,
    itemIds: List<Int>,
    onItemChange: (Inventory.Item) -> Unit,
    onCloseRequest: () -> Unit
) {
    var quantity by state {
        item.quantity.coerceIn(1, maxAllowedQuantity)
    }

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        // show current item name
        if (item.id != 0) {
            Text(
                text = itemNames[item.id],
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }

        Row(
            verticalGravity = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Quantity",
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            value = quantity.toFloat(),
            valueRange = 1f..maxAllowedQuantity.toFloat(),
            onValueChange = { quantity = it.roundToInt() },
            onValueChangeEnd = { onItemChange(item.copy(quantity = quantity)) }
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Choose an item",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium)
        )
        AdapterList(data = itemIds) { id ->
            ListItem(
                text = itemNames[id],
                onClick = {
                    onItemChange(item.copy(id = id, quantity = quantity))
                    onCloseRequest()
                }
            )
        }
    }
}

@Composable
private fun InventoryTypes(types: List<Inventory.Type>, onTypeChange: (Inventory.Type) -> Unit) {
    val resources = PokemonResourcesAmbient.current.items
    var selectedIndex by state { 0 }
    TabRow(
        backgroundColor = translucentSurfaceColor(),
        items = types,
        modifier = Modifier.zIndex(8f).fillMaxWidth(),
        selectedIndex = selectedIndex,
        scrollable = false // TODO: this should be scrollable
    ) { index, type ->
        Tab(
            modifier = Modifier.preferredHeight(48.dp),
            text = { Text(text = resources.getTypeName(type)) },
            selected = index == selectedIndex,
            onSelected = {
                onTypeChange(type)
                selectedIndex = index
            }
        )
    }
}

@Composable
private fun ItemsList(
    items: List<Inventory.Item>,
    names: List<String>,
    onItemClick: (item: Inventory.Item) -> Unit
) {
    AdapterList(data = items) { item ->
        ListItem(
            text = { Text(text = names[item.id]) },
            secondaryText = { Text(text = "Qt. ${item.quantity}") },
            onClick = { onItemClick(item) }
        )
        Divider()
    }
}

@Preview
@Composable
private fun PreviewItemsList() {
    PreviewScreen(colors = LightColors) {
        // TODO: show items' sprite
        ItemsList(
            items = List(3) { Inventory.Item(it, 0, 99) },
            names = listOf("MasterBall"),
            onItemClick = {}
        )
    }
}

@Preview
@Composable
private fun PreviewItemEditor() {
    PreviewScreen(colors = LightColors) {
        ItemEditor(
            item = Inventory.Item(0, 1, 85),
            itemNames = List(10) { "Master Ball" },
            itemIds = List(10) { it },
            onItemChange = {},
            onCloseRequest = {},
            maxAllowedQuantity = 99
        )
    }
}
