package com.manueldidonna.pk.core

interface Bag {
    /**
     * Every game supports a limited set of types.
     */
    val inventoryTypes: Set<Inventory.Type>

    /**
     * Return a [Inventory] instance.
     *
     * Should throw an [IllegalStateException] if [type] isn't included in [inventoryTypes]
     */
    operator fun get(type: Inventory.Type): Inventory
}
