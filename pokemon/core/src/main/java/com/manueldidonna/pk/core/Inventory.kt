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
     * These 'special items' are represented by unused ids:
     * - [BikeVoucherId]
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

            /**
             * A voucher for obtaining a bicycle from the Bike Shop.
             * Available in R/B/Y & FR/LG
             */
            const val BikeVoucherId = 129
        }
    }
}

fun Inventory.getItem(index: Int): Inventory.Item {
    return selectItem(index, mapTo = Inventory.Item::Immutable)
}

fun Inventory.Item.toImmutable(
    index: Int = this.index,
    id: Int = this.id,
    quantity: Int = this.quantity
): Inventory.Item.Immutable {
    return Inventory.Item.Immutable(index = index, id = id, quantity = quantity)
}
