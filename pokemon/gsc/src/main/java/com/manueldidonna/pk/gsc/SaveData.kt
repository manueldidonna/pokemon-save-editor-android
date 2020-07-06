package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.core.Inventory as CoreInventory
import com.manueldidonna.pk.core.SaveData as CoreSaveData

internal class SaveData(
    override val version: Version,
    private val data: UByteArray
) : CoreSaveData {

    override val trainer: Trainer
        get() = TODO("Not yet implemented")

    override val pokedex: Pokedex by lazy { Pokedex(data, version) }

    override val supportedInventoryTypes: List<CoreInventory.Type> = listOf(
        CoreInventory.Type.General,
        CoreInventory.Type.Balls,
        CoreInventory.Type.TechnicalMachines,
        CoreInventory.Type.HiddenMachines,
        CoreInventory.Type.Keys,
        CoreInventory.Type.Computer
    )

    override fun getInventory(type: CoreInventory.Type): Inventory {
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