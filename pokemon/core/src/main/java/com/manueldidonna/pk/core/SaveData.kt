package com.manueldidonna.pk.core

interface SaveData : StorageSystem {
    val version: Version

    val trainer: Trainer

    val pokedex: Pokedex

    val bag: Bag

    fun exportToBytes(): UByteArray

    interface Factory {
        fun create(data: UByteArray): SaveData?
    }
}
