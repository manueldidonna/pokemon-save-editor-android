package com.manueldidonna.pk.core

interface SaveData {
    val version: Version

    val trainer: Trainer

    val pokedex: Pokedex

    val bag: Bag

    val storageSystem: StorageSystem

    fun exportToBytes(): UByteArray

    interface Factory {
        fun create(data: UByteArray): SaveData?
    }
}
