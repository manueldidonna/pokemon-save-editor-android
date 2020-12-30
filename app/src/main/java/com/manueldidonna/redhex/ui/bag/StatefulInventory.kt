package com.manueldidonna.redhex.ui.bag

import android.util.Log
import androidx.compose.runtime.*
import com.manueldidonna.pk.core.Bag
import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.Item
import com.manueldidonna.redhex.common.rememberMutableStateFor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun rememberStatefulInventory(bag: Bag, type: Inventory.Type): StatefulInventory {
    val itemMapper = inventoryItemMapper()
    val inventory = rememberMutableStateFor(type) {
        StatefulInventory(bag[type], itemMapper)
    }
    LaunchedEffect(subject = inventory) {
        withContext(Dispatchers.Default) {
            inventory.value.fetchItems()
        }
    }
    return inventory.value
}

@Stable
class StatefulInventory(
    private val inventory: Inventory,
    private val itemMapper: Inventory.ItemMapper<InventoryItem>,
) : Inventory by inventory {

    private val _items = mutableStateListOf<InventoryItem>()

    val items: List<InventoryItem> = _items

    override val size: Int by derivedStateOf { _items.size }

    fun fetchItems() {
        if (_items.isNotEmpty()) _items.clear()
        _items.addAll(List(size = inventory.size) {
            inventory.selectItem(it, itemMapper)
        })
    }

    override fun setItem(index: Int, item: Item) {
        inventory.setItem(index, item)
        val newItem = inventory.selectItem(index, itemMapper)
        if (_items.size <= index) _items.add(newItem) else _items[index] = newItem
    }

    override fun removeItemAt(index: Int) {
        inventory.removeItemAt(index)
        if (index < _items.size) {
            _items.removeAt(index)
        }
    }
}
