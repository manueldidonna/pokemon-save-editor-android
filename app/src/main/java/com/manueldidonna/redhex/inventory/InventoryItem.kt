package com.manueldidonna.redhex.inventory

import androidx.compose.foundation.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.toImmutable
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.ui.LightColors
import com.manueldidonna.redhex.common.ui.PreviewScreen
import kotlin.math.roundToInt

@Composable
fun InventoryItemEditor(
    item: Inventory.Item,
    maxAllowedQuantity: Int,
    resources: PokemonTextResources.Items,
    itemIds: List<Int>,
    onItemChange: (Inventory.Item) -> Unit
) {
    val isEditing = remember { item.id > 0 }

    val quantity = remember {
        mutableStateOf(item.quantity.coerceIn(1, maxAllowedQuantity), structuralEqualityPolicy())
    }

    val itemId = remember {
        mutableStateOf(item.id.coerceAtLeast(0), structuralEqualityPolicy())
    }

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        // show current item name
        if (itemId.value > 0) {
            Text(
                text = resources.getAllItems()[itemId.value],
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }
        if (maxAllowedQuantity > 1) {
            Quantity(
                maxAllowedQuantity = maxAllowedQuantity,
                quantity = quantity.value,
                onQuantityChange = { quantity.value = it }
            )
            Divider()
        }
        Box(modifier = Modifier.weight(1f)) {
            ItemsList(
                itemIds = itemIds,
                itemNames = resources.getAllItems(),
                selectedItemId = itemId.value,
                onSelectionChange = { itemId.value = it }
            )
        }
        Divider()
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().padding(end = 16.dp, top = 8.dp)
        ) {
            if (isEditing) {
                TextButton(onClick = { onItemChange(Inventory.Item.empty(item.index)) }) {
                    Text(text = "Delete")
                }
            }
            Spacer(Modifier.width(8.dp))
            TextButton(
                enabled = isEditing || itemId.value != 0,
                onClick = {
                    onItemChange(item.toImmutable(quantity = quantity.value, id = itemId.value))
                },
                content = { Text(text = if (isEditing) "Modify" else "Add") }
            )
        }
    }
}

@Composable
private fun Quantity(maxAllowedQuantity: Int, quantity: Int, onQuantityChange: (Int) -> Unit) {
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
        onValueChange = { onQuantityChange(it.roundToInt()) }
    )
}

@Composable
private fun ItemsList(
    itemIds: List<Int>,
    itemNames: List<String>,
    selectedItemId: Int,
    onSelectionChange: (id: Int) -> Unit
) {
    // TODO: add a search field
    val items: List<Pair<Int, String>> = remember {
        itemIds.map { Pair(it, itemNames[it]) }.sortedBy { it.second }
    }

    LazyColumnFor(items = items, itemContent = { itemWithName ->
        val selected = selectedItemId == itemWithName.first
        val onSelect = { onSelectionChange(itemWithName.first) }
        Box(modifier = Modifier.selectable(selected = selected, onClick = onSelect)) {
            Row(Modifier.fillParentMaxWidth().padding(16.dp)) {
                RadioButton(selected = selected, onClick = onSelect)
                Text(
                    text = itemWithName.second,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    })
}

@Preview
@Composable
private fun PreviewItemEditor() {
    val fakeResources = object : PokemonTextResources.Items {
        override fun getAllItems(): List<String> = List(10) { "Master Ball" }
        override fun getTypeName(type: Inventory.Type): String = "General"
    }

    PreviewScreen(colors = LightColors) {
        InventoryItemEditor(
            item = Inventory.Item.Immutable(0, 1, 85),
            resources = fakeResources,
            itemIds = List(10) { it },
            onItemChange = {},
            maxAllowedQuantity = 99
        )
    }
}
