package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.Inventory.Type
import com.manueldidonna.pk.core.Bag as CoreBag

internal class Bag(internal val data: UByteArray) : CoreBag {
    override fun toString(): String = "RBY Bag"

    override val inventoryTypes = setOf(Type.General, Type.Computer)

    override fun get(type: Type): Inventory {
        require(inventoryTypes.contains(type)) {
            "Type $type is not supported"
        }
        val isComputer = type == Type.Computer
        return Inventory(
            type = type,
            data = data,
            capacity = if (isComputer) 50 else 20,
            startOffset = if (isComputer) ComputerOffset else ItemsOffset
        )
    }

    companion object {
        private const val ComputerOffset = 0x27E6
        private const val ItemsOffset = 0x25C9
    }
}
