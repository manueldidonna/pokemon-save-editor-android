package com.manueldidonna.pk.core

/**
 * The Inventory holds all of the trainer's items
 *
 * @see Item
 */
interface Inventory {
    val type: Type

    /**
     * Every game supports a limited set of types.
     *
     * @see Bag.inventoryTypes
     */
    enum class Type {
        General,
        Computer,
        Balls,
        Keys,
        HiddenMachines,
        TechnicalMachines
    }

    /**
     * List of ids that this inventory recognizes
     */
    val supportedItemIds: List<Int>

    /**
     * It is the value to which all [Item.quantity] are coerced
     */
    val maxQuantity: Int // TODO: rename to maxQuantityPerItem

    /**
     * The maximum number of [Item] that can fit in this inventory.
     */
    val capacity: Int

    /**
     * Current number of [Item] in this inventory.
     */
    val size: Int

    /**
     * Map [Item] properties to [I] with [mapper].
     * The passed [index] cannot be grater than [capacity].
     *
     * @see ItemMapper
     */
    fun <I> selectItem(index: Int, mapper: ItemMapper<I>): I

    /**
     * Used to map [Item] properties to a generic type [I].
     *
     * @see Item
     */
    fun interface ItemMapper<I> {
        fun mapTo(id: Int, quantity: Int): I
    }

    /**
     * Set the passed [item] in the inventory:
     * - [Item.id] must be included in [supportedItemIds]
     * - [index] cannot be greater than [capacity] or negative
     * - [Item] cannot be empty
     *
     * @see Item
     * @see Inventory.removeItemAt
     * @see Item.isEmpty
     */
    fun setItem(index: Int, item: Item)

    /**
     * Remove an item by [index]
     *
     * TODO: return boolean or Item?
     */
    fun removeItemAt(index: Int)
}

inline val Inventory.Type.isMachinesType: Boolean
    get() = this == Inventory.Type.HiddenMachines || this == Inventory.Type.TechnicalMachines

inline val Inventory.isFull: Boolean get() = size == capacity

inline val Inventory.isEmpty: Boolean get() = size <= 0

/**
 * Create an [Item] instance from the passed properties and set it in the inventory.
 * Return the [Item] set in the inventory.
 *
 * @see Inventory.setItem
 */
fun Inventory.setItem(index: Int, id: Int, quantity: Int): Item {
    val item = object : Item {
        override val id: Int = id
        override val quantity: Int = quantity.coerceIn(1, maxQuantity)
    }
    require(!item.isEmpty) { "Item cannot be empty" }
    setItem(index, item)
    return item
}

fun Inventory.stackItem(index: Int, item: Item) {
    // ignore empty items or invalid indices
    if (item.isEmpty || index < 0) return

    // the item cannot be stacked
    if (item.quantity >= maxQuantity || isEmpty)
        return setItem(index, item)

    // The inventory already contains the passed item in the same slot,
    // so the user is editing the item quantity
    if (selectItem(index) { id, _ -> id == item.id })
        return setItem(index, item)

    var quantityToStack = item.quantity
    for (@Suppress("NAME_SHADOWING") index in 0 until size) {
        selectItem(index) { id, quantity ->
            // get the items with the same id of the passed one
            if (id != item.id || quantity >= maxQuantity) return@selectItem
            val newItem = setItem(index, id, quantity = quantityToStack + quantity)
            quantityToStack -= newItem.quantity - quantity
        }
        if (quantityToStack <= 0) break
    }

    if (quantityToStack > 0) {
        // insert the item without stacking it
        setItem(index = index, id = item.id, quantity = quantityToStack)
    } else {
        // the user is editing the item id but the quantity has been added to other slots
        removeItemAt(index)
    }
}
