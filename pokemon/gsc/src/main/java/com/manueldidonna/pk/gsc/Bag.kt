package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.Inventory.Type
import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.gsc.converter.*
import com.manueldidonna.pk.core.Bag as CoreBag

internal class Bag(
    internal val data: UByteArray,
    internal val version: Version,
) : CoreBag {

    override fun hashCode(): Int {
        return version.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (this.version != (other as Bag).version) return false
        return this.data.contentEquals(other.data)
    }

    override fun toString(): String {
        return "GSC Bag => Version: $version"
    }

    override val inventoryTypes = setOf(
        Type.General,
        Type.Balls,
        Type.TechnicalMachines,
        Type.HiddenMachines,
        Type.Keys,
        Type.Computer
    )

    override fun get(type: Type): Inventory {
        require(inventoryTypes.contains(type)) { "Type $type is not supported" }
        val startDataOffset: Int
        val capacity: Int
        when (type) {
            Type.Keys -> {
                startDataOffset = if (version == Version.Crystal) 0x244A else 0x2449
                capacity = 26
            }
            Type.Computer -> {
                startDataOffset = if (version == Version.Crystal) 0x247F else 0x247E
                capacity = 50
            }
            Type.Balls -> {
                startDataOffset = if (version == Version.Crystal) 0x2465 else 0x2464
                capacity = 12
            }
            Type.General -> {
                startDataOffset = if (version == Version.Crystal) 0x2420 else 0x241F
                capacity = 20
            }
            Type.HiddenMachines -> {
                startDataOffset = (if (version == Version.Crystal) 0x23E7 else 0x23E6) + 0x32
                capacity = 7
            }
            Type.TechnicalMachines -> {
                startDataOffset = if (version == Version.Crystal) 0x23E7 else 0x23E6
                capacity = 50
            }
        }
        return Inventory(
            data = data,
            startDataOffset = startDataOffset,
            type = type,
            capacity = capacity,
            supportedItemIds = getIdsByType(type, isCrystal = version == Version.Crystal),
            maxQuantity = if (type == Type.HiddenMachines || type == Type.Keys) 1 else 99
        )
    }

    private fun getIdsByType(type: Type, isCrystal: Boolean): List<Int> {
        return when (type) {
            Type.General -> Items
            Type.Computer -> if (isCrystal) (AllItems + CrystalExclusiveKeys) else AllItems
            Type.Balls -> Balls
            Type.Keys -> if (isCrystal) (Keys + CrystalExclusiveKeys) else Keys
            Type.HiddenMachines -> HiddenMachines
            Type.TechnicalMachines -> TechnicalMachines
            else -> throw IllegalArgumentException("Unsupported type $type")
        }
    }
}
