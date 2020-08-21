package com.manueldidonna.pk.core

interface SaveData : StorageSystem {
    val version: Version

    val trainer: Trainer

    val pokedex: Pokedex

    val supportedInventoryTypes: List<Inventory.Type>

    fun getInventory(type: Inventory.Type): Inventory

    fun exportToBytes(): UByteArray

    interface Factory {
        fun createSaveData(data: UByteArray): SaveData?
    }
}
