package com.manueldidonna.redhex.inventory

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.SpritesRetriever

@Stable
class ObservableInventory(
    private val inventory: Inventory,
    resources: PokemonTextResources.Items,
    spritesRetriever: SpritesRetriever,
) : Inventory by inventory {

    private val itemNames = resources.getAllItems()

    private val inventoryItem: (Int, Int, Int) -> InventoryItem = { index, id, quantity ->
        InventoryItem(index, id, quantity, itemNames[id], spritesRetriever.getItemSprite(id))
    }

    val items: State<List<InventoryItem>> = mutableStateOf(emptyList())

    override fun setItem(item: Inventory.Item, index: Int) {
        inventory.setItem(item, index)
        fetchAllItems()
    }

    fun fetchAllItems() {
        (items as MutableState).value = List(size) { selectItem(it, mapTo = inventoryItem) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (this.type != (other as ObservableInventory).type) return false
        if (this.size != other.size) return false
        if (this.capacity != other.capacity) return false
        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + capacity.hashCode()
        result = 31 * result + size.hashCode()
        return result
    }
}
