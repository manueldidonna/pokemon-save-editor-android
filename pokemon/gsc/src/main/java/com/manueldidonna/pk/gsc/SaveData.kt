package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.core.SaveData as CoreSaveData

internal class SaveData(
    override val version: Version,
    private val data: UByteArray
) : CoreSaveData {
    override var trainer: Trainer
        get() = TODO("Not yet implemented")
        set(value) {}

    override val pokedex: Pokedex by lazy { Pokedex(data, version) }

    override val supportedInventoryTypes: List<Inventory.Type> = listOf(
        Inventory.Type.General,
        Inventory.Type.Balls,
        Inventory.Type.Machines,
        Inventory.Type.Keys,
        Inventory.Type.Computer
    )

    override fun getInventory(type: Inventory.Type): Inventory {
        TODO("Not yet implemented")
    }

    override val indices: IntRange = StorageCollection.PartyIndex until 14

    override fun getStorage(index: Int): Storage {
        TODO("Not yet implemented")
    }

    override fun getMutableStorage(index: Int): MutableStorage {
        TODO("Not yet implemented")
    }

    override fun exportToBytes(): UByteArray {
        TODO("Not yet implemented")
    }
}