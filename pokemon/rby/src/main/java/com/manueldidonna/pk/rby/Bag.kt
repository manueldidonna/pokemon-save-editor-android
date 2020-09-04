package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.Bag as CoreBag

internal class Bag(internal val data: UByteArray) : CoreBag {

    override fun hashCode(): Int {
        return data.contentHashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        return this.data.contentEquals((other as Bag).data)
    }

    override fun toString(): String {
        return "RBY Bag"
    }

    override val inventoryTypes = setOf(Inventory.Type.General, Inventory.Type.Computer)

    override fun get(type: Inventory.Type): Inventory {
        require(inventoryTypes.contains(type)) { "Type $type is not supported" }
        val isComputer = type == Inventory.Type.Computer
        return Inventory(
            type = type,
            data = data,
            capacity = if (isComputer) 50 else 20,
            startOffset = if (isComputer) 0x27E6 else 0x25C9
        )
    }
}
