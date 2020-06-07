package com.manueldidonna.pk.core

/**
 * The Inventory holds all of the trainer's items
 */
interface Inventory {
    /**
     * Every game supports a limited set of [Type]s
     * @see [SaveData.supportedInventoryTypes]
     */
    val type: Type

    enum class Type {
        General,
        Computer,
        Balls,
        Keys
    }

    /**
     * List of ids that an Inventory instance recognizes
     */
    val supportedItemIds: List<Int>

    /**
     * It is the value to which all [Item.quantity] are coerced
     */
    val maxAllowedQuantity: Int

    /**
     * Max allowed number of items in the Inventory
     */
    val capacity: Int

    /**
     * The total number of items available in the Inventory.
     */
    val size: Int


    /**
     * Should throw an [IllegalStateException] if [index] is greater than [capacity]
     */
    fun getItem(index: Int): Item

    /**
     * Should throw an [IllegalStateException]
     * if [item] id isn't included in [supportedItemIds]
     * or [index] is greater than [capacity]
     */
    fun setItem(item: Item, index: Int = item.index)

    data class Item(
        val index: Int,
        val id: Int,
        val quantity: Int
    ) {
        companion object {
            fun empty(index: Int): Item = Item(index = index, id = 0, quantity = 0)
        }
    }
}

fun Inventory.getAllItems(): List<Inventory.Item> {
    return List(size) { getItem(it) }
}
