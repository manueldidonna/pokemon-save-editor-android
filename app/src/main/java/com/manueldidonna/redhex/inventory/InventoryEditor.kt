package com.manueldidonna.redhex.inventory

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.toImmutable
import com.manueldidonna.redhex.common.PokemonResourcesAmbient
import com.manueldidonna.redhex.common.rememberMutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun InventoryEditor(
    item: Inventory.Item,
    maxAllowedQuantity: Int,
    itemIds: List<Int>,
    onItemChange: (Inventory.Item) -> Unit,
) {
    val isEditing = remember { item.id > 0 }
    var quantity by rememberMutableState { item.quantity.coerceIn(1, maxAllowedQuantity) }
    var itemId by rememberMutableState { item.id.coerceAtLeast(0) }

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        if (itemId > 0) {
            SelectedItem(itemId = itemId)
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }
        if (maxAllowedQuantity > 1) {
            ItemQuantity(
                maxAllowedQuantity = maxAllowedQuantity,
                quantity = quantity,
                onQuantityChange = { quantity = it }
            )
            Divider()
        }
        ItemsList(
            modifier = Modifier.weight(1f),
            itemIds = itemIds,
            selectedItemId = itemId,
            onSelectionChange = { itemId = it }
        )
        Divider()
        EditorActions(
            canDeleteItem = isEditing,
            canSetItem = isEditing || itemId != 0,
            deleteItem = { onItemChange(Inventory.Item.empty(item.index)) },
            setItem = { onItemChange(item.toImmutable(quantity = quantity, id = itemId)) }
        )
    }
}

@Composable
private fun SelectedItem(itemId: Int) {
    val resources = PokemonResourcesAmbient.current.items
    Text(
        text = resources.getAllItems()[itemId],
        style = MaterialTheme.typography.h6,
        modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
    )
}

@Composable
private fun EditorActions(
    canDeleteItem: Boolean,
    canSetItem: Boolean,
    deleteItem: () -> Unit,
    setItem: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth().padding(end = 16.dp, top = 8.dp)
    ) {
        if (canDeleteItem)
            TextButton(onClick = deleteItem) {
                Text(text = "Delete")
            }
        Spacer(Modifier.width(8.dp))
        TextButton(
            enabled = canSetItem,
            onClick = setItem,
            content = { Text(text = if (canDeleteItem) "Modify" else "Add") }
        )
    }
}

@Composable
private fun ItemQuantity(
    maxAllowedQuantity: Int,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
) {
    Row(
        verticalGravity = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Quantity",
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Medium,
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
        onValueChange = { onQuantityChange(it.roundToInt()) }
    )
}

@Immutable
private data class ItemEntry(val itemId: Int, val itemName: String)

@Composable
private fun ItemsList(
    modifier: Modifier,
    itemIds: List<Int>,
    selectedItemId: Int,
    onSelectionChange: (id: Int) -> Unit,
) {
    val itemNames = PokemonResourcesAmbient.current.items.getAllItems()
    val entries = rememberMutableState(referentialEqualityPolicy()) { listOf<ItemEntry>() }
    launchInComposition {
        withContext(Dispatchers.IO) {
            entries.value = itemIds.map { ItemEntry(it, itemNames[it]) }.sortedBy { it.itemName }
        }
    }
    LazyColumnFor(items = entries.value, modifier = modifier) { entry ->
        Item(entry, selectedItemId == entry.itemId, onSelectionChange)
    }
}

@Composable
private fun Item(
    entry: ItemEntry,
    selected: Boolean,
    onSelectionChange: (itemId: Int) -> Unit,
) {
    val callback = { onSelectionChange(entry.itemId) }
    Row(modifier = Modifier
        .fillMaxWidth()
        .selectable(selected = selected, onClick = callback)
        .padding(16.dp)
    ) {
        RadioButton(selected = selected, onClick = callback)
        Text(
            text = entry.itemName,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
