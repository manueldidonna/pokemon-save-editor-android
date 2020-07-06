package com.manueldidonna.pk.core

interface SaveData : StorageCollection {
    val version: Version

    var trainer: Trainer // TODO: make trainer immutable

    val pokedex: Pokedex

    val supportedInventoryTypes: List<Inventory.Type>

    fun getInventory(type: Inventory.Type): Inventory

    fun exportToBytes(): UByteArray

    interface Factory {
        fun createSaveData(data: UByteArray): SaveData?
    }
}
