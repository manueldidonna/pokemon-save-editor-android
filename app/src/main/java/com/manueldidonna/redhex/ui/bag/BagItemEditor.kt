package com.manueldidonna.redhex.ui.bag

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material.icons.twotone.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Item
import com.manueldidonna.redhex.AmbientPokemonTextResources
import com.manueldidonna.redhex.DefaultIcons
import com.manueldidonna.redhex.PokemonEditorTheme
import com.manueldidonna.redhex.common.RadioButton
import com.manueldidonna.redhex.common.SurfaceDialog
import com.manueldidonna.redhex.common.rememberMutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun BagItemEditorDialog(
    allItemIds: List<Int>,
    quantityRange: IntRange,
    itemToEdit: Item?,
    onSelectionChange: (Item) -> Unit,
    deleteSelection: () -> Unit
) {
    var itemQuantity by rememberMutableState { itemToEdit?.quantity ?: quantityRange.first }
    var itemId by rememberMutableState { itemToEdit?.id ?: -1 }

    val selectedBagItem by derivedStateOf {
        Item.Immutable(id = itemId, quantity = itemQuantity)
    }

    val items = rememberItemWithNames(allItemIds)
    if (items.isEmpty()) return

    SurfaceDialog(onDismissRequest = { onSelectionChange(selectedBagItem) }) {
        Column {
            ItemQuantityCounter(
                enabled = itemId >= 0,
                quantity = itemQuantity,
                onChange = { itemQuantity = it.coerceIn(quantityRange) }
            )
            Divider()
            ItemsList(
                modifier = Modifier.weight(1f),
                items = items,
                selectedId = itemId,
                onChange = { itemId = it }
            )
            Divider()
            TextButton(
                enabled = itemId >= 0,
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                onClick = deleteSelection
            ) {
                Text(text = "DELETE SELECTED ITEM")
            }
        }
    }
}

@Immutable
private data class ItemWithName(val id: Int, val name: String)

@Composable
@Stable
private fun rememberItemWithNames(itemIds: List<Int>): List<ItemWithName> {
    val itemsTextResources = AmbientPokemonTextResources.current.items
    val items = produceState(emptyList<ItemWithName>()) {
        value = withContext(Dispatchers.Default) {
            itemIds
                .map { ItemWithName(it, itemsTextResources.getItemById(it)) }
                .sortedBy { it.name }
        }
    }
    return items.value
}

@Composable
private fun ItemsList(
    modifier: Modifier,
    items: List<ItemWithName>,
    selectedId: Int,
    onChange: (Int) -> Unit
) {
    if (items.isEmpty()) return
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        if (selectedId <= 0) return@LaunchedEffect
        listState.snapToItemIndex(index = items.indexOfFirst { it.id == selectedId })
    }
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
    ) {
        items(items) {
            RadioButton(
                text = it.name,
                selected = it.id == selectedId,
                onClick = { onChange(it.id) },
                modifier = Modifier.fillMaxWidth(),
                horizontalPadding = 16.dp
            )
        }
    }
}

@Composable
private fun ItemQuantityCounter(
    enabled: Boolean,
    quantity: Int,
    onChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = "Quantity",
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.body2,
            color = AmbientContentColor.current.copy(alpha = ContentAlpha.medium),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.h6,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onChange(quantity + 1) }, enabled = enabled) {
                Icon(DefaultIcons.Add, tint = MaterialTheme.colors.primary)
            }

            IconButton(onClick = { onChange(quantity - 1) }, enabled = enabled) {
                Icon(DefaultIcons.Remove, tint = MaterialTheme.colors.primary)
            }
        }
    }
}


@Preview
@Composable
private fun Preview() {
    PokemonEditorTheme {
        Surface {
            ItemQuantityCounter(
                enabled = true,
                quantity = 5,
                onChange = {}
            )
        }
    }
}