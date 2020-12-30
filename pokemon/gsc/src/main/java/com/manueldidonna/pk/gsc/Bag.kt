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

    override fun toString(): String = "Bag for $version"

    override fun hashCode() = version.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (this.version != (other as Bag).version) return false
        return true
    }

    private val offsets: Offsets =
        if (version == Version.Crystal) Offsets.InternationalCrystal
        else Offsets.InternationalGoldSilver

    override val inventoryTypes = setOf(
        Type.General,
        Type.Balls,
        Type.TechnicalMachines,
        Type.HiddenMachines,
        Type.Keys,
        Type.Computer
    )

    override fun get(type: Type): Inventory {
        require(inventoryTypes.contains(type)) {
            "Type $type is not supported"
        }
        return Inventory(
            data = data,
            startOffset = getInventoryOffset(type),
            type = type,
            capacity = getCapacity(type),
            supportedItemIds = getSupportedItemsIds(type),
            maxQuantity = getMaxQuantityPerItem(type)
        )
    }

    private fun getSupportedItemsIds(type: Type): List<Int> {
        val isCrystal = version == Version.Crystal
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

    private fun getCapacity(type: Type): Int {
        return when (type) {
            Type.General -> 20
            Type.Computer -> 50
            Type.Balls -> 12
            Type.Keys -> 26
            Type.HiddenMachines -> 7
            Type.TechnicalMachines -> 50
            else -> throw IllegalArgumentException("Unsupported type $type")
        }
    }

    private fun getInventoryOffset(type: Type): Int {
        return when (type) {
            Type.General -> offsets.items
            Type.Computer -> offsets.computer
            Type.Balls -> offsets.balls
            Type.Keys -> offsets.keys
            Type.HiddenMachines -> offsets.hiddenMachines
            Type.TechnicalMachines -> offsets.technicalMachines
        }
    }

    private fun getMaxQuantityPerItem(type: Type): Int {
        return if (type == Type.HiddenMachines || type == Type.Keys) 1 else 99
    }

    private sealed class Offsets {
        abstract val keys: Int
        abstract val computer: Int
        abstract val balls: Int
        abstract val items: Int
        abstract val hiddenMachines: Int
        abstract val technicalMachines: Int

        object InternationalCrystal : Offsets() {
            override val balls = 0x2465
            override val computer = 0x247F
            override val items = 0x2420
            override val hiddenMachines = 0x23E7 + 0x32
            override val technicalMachines = 0x23E7
            override val keys = 0x244A
        }

        object InternationalGoldSilver : Offsets() {
            override val balls = 0x2464
            override val computer = 0x247E
            override val items = 0x241F
            override val hiddenMachines = 0x23E6 + 0x32
            override val technicalMachines = 0x23E6
            override val keys = 0x2449
        }
    }
}
