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
        Keys,
        HiddenMachines,
        TechnicalMachines
    }

    /**
     * List of ids that an Inventory instance recognizes
     */
    val supportedItemIds: List<Int>

    /**
     * It is the value to which all [Item.quantity] are coerced
     */
    val maxQuantity: Int

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
    fun <T> selectItem(index: Int, mapTo: (index: Int, id: Int, quantity: Int) -> T): T

    /**
     * Should throw an [IllegalStateException]
     * if [item] id isn't included in [supportedItemIds]
     * or [index] is greater than [capacity]
     */
    fun setItem(item: Item, index: Int = item.index)

    /**
     * An [Item] is an object deposited in the Inventory. It may be carried by a Pokemon.
     *
     * Some items are specific to a game or should be treated specially.
     * These 'special items' are listed in [Items]
     * @see Items
     */
    interface Item {
        val index: Int
        val id: Int
        val quantity: Int

        /**
         * Used to represent an immutable [Item].
         * Use [Item.toImmutable] to get an instance of [Immutable] from every [Item] instance
         */
        data class Immutable(
            override val index: Int,
            override val id: Int,
            override val quantity: Int
        ) : Item

        companion object {
            /**
             * Pass this instance to [Inventory.setItem] to delete the item in [index] position.
             */
            fun empty(index: Int): Item = Immutable(index = index, id = 0, quantity = 0)

            /**
             * A Hidden Machine is an item that is used to teach a Pokemon a move.
             * HMs can be used an unlimited number of times and cannot be disposed of.
             */
            fun isHiddenMachine(itemId: Int): Boolean {
                return itemId == 737 || itemId in 420..427
            }

            /**
             * A Technical Machine is an item that can be used to teach a Pokemon a move.
             */
            fun isTechnicalMachine(itemId: Int): Boolean {
                return itemId == 1230 || itemId in 328..419 || itemId in 618..620 || itemId in 690..694
            }
        }
    }
}

/**
 * Returns true if the [Inventory] is full
 */
inline val Inventory.isFull: Boolean
    get() = size == capacity

/**
 * Get an instance of [Inventory.Item] from the Inventory at [index]
 */
fun Inventory.getItem(index: Int): Inventory.Item {
    return selectItem(index, mapTo = Inventory.Item::Immutable)
}

/**
 * Execute an action with the id and the quantity of an item at [index]
 *
 * The same limitations of [Inventory.selectItem] are applied to this function as well
 */
inline fun Inventory.withItem(index: Int, crossinline block: (id: Int, quantity: Int) -> Unit) {
    selectItem(index) { _, id, quantity -> block(id, quantity) }
}

/**
 * Insert the [item] into the Inventory, allowing to stack its quantity
 * if it already exists at a different index [Inventory.Item.index]
 */
fun Inventory.stackItem(item: Inventory.Item) {
    // fast path, max quantity can't be added to an existent item, or the inventory/item is empty
    if (item.quantity == maxQuantity || size == 0 || item.quantity <= 0 || item.id == 0) {
        setItem(item)
        return
    }
    var itemQuantity = item.quantity
    for (i in 0 until size) {
        // check if exists an item with the same id
        withItem(index = i) { id, quantity ->
            if (id == item.id) {
                // if the index is the same, the user is editing an existing item,
                // so consume the quantity and overwrite the item
                if (i == item.index) {
                    setItem(item)
                    itemQuantity = 0
                } else if (quantity < maxQuantity) {
                    val newItemQuantity = (itemQuantity + quantity).coerceAtMost(maxQuantity)
                    setItem(Inventory.Item.Immutable(i, id, newItemQuantity))
                    itemQuantity -= newItemQuantity - quantity
                }
            }
        }
        if (itemQuantity <= 0) {
            // if the passed item is stacked in a different index than item.index
            // delete the item at item.index
            val isItemEdited = selectItem(item.index) { _, id, _ -> id == item.id }
            if (!isItemEdited) {
                setItem(Inventory.Item.empty(item.index))
            }
            return
        }
    }
    // insert the item in a new slot of the inventory
    setItem(item.toImmutable(quantity = itemQuantity))
}

/**
 * Convert a generic [Inventory.Item] implementation to an [Inventory.Item.Immutable] data class
 */
fun Inventory.Item.toImmutable(
    index: Int = this.index,
    id: Int = this.id,
    quantity: Int = this.quantity,
): Inventory.Item.Immutable {
    return Inventory.Item.Immutable(index = index, id = id, quantity = quantity)
}
