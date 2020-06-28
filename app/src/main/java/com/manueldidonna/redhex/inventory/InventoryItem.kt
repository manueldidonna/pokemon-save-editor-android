package com.manueldidonna.redhex.inventory

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Text
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.text.font.FontWeight
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
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
    onItemChange: (Inventory.Item?) -> Unit
) {
    val isEditing = remember { item.id > 0 }

    var quantity by state { item.quantity.coerceIn(1, maxAllowedQuantity) }
    var itemId by state { item.id.coerceAtLeast(0) }

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        // show current item name
        if (isEditing) {
            Text(
                text = resources.getAllItems()[itemId],
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }
        Quantity(
            maxAllowedQuantity = maxAllowedQuantity,
            quantity = quantity,
            onQuantityChange = { quantity = it }
        )
        Divider()
        Box(modifier = Modifier.weight(1f)) {
            ItemsList(
                itemIds = itemIds,
                itemNames = resources.getAllItems(),
                selectedItemId = itemId,
                onSelectionChange = { itemId = it }
            )
        }
        Divider()
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().padding(end = 16.dp, top = 8.dp)
        ) {
            TextButton(onClick = { onItemChange(null) }) {
                Text(text = "Cancel")
            }
            Spacer(Modifier.width(8.dp))
            TextButton(
                enabled = isEditing || itemId != 0,
                onClick = {
                    onItemChange(item.toImmutable(quantity = quantity, id = itemId))
                }
            ) {
                if(isEditing) {
                    Text(text = if(itemId == 0) "Delete" else "Modify")
                } else {
                    Text(text = "Add")
                }
            }
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

    RadioGroup {
        LazyColumnItems(items = items, itemContent = { itemWithName ->
            val selected = selectedItemId == itemWithName.first
            RadioGroupTextItem(
                selected = selected,
                onSelect = { onSelectionChange(itemWithName.first) },
                text = itemWithName.second
            )
        })
    }
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
