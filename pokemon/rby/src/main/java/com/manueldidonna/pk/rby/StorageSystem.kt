package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.Storage
import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.core.StorageSystem as CoreStorageSystem

internal class StorageSystem(
    private val data: UByteArray,
    private val version: Version,
) : CoreStorageSystem {
    override val storageIndices: IntRange = CoreStorageSystem.PartyIndex until 12

    override fun set(index: Int, storage: Storage) {
        require(index in storageIndices) { "Index $index is out of bounds [$storageIndices]" }
        require(storage is com.manueldidonna.pk.rby.Storage) { "Invalid Storage class" }
        val import = storage.exportToBytes()
        require(import.size == getStorageSize(index)) {
            "Storage size ${storage.size} is incompatible with the index $index"
        }
        import.copyInto(data, getStorageOffset(index))
    }

    override fun get(index: Int): Storage {
        require(index in storageIndices) { "Index $index is out of bounds [$storageIndices]" }
        val isParty = index == CoreStorageSystem.PartyIndex
        return Storage(
            data = getStorageData(index),
            storageIndex = index,
            capacity = if (isParty) 6 else 20,
            version = version,
            name = if (isParty) "Party" else "Box ${index + 1}"
        )
    }

    private fun getStorageData(index: Int): UByteArray {
        val offset = getStorageOffset(index)
        return data.copyOfRange(offset, offset + getStorageSize(index))
    }

    private fun getStorageSize(index: Int): Int {
        return if (index == CoreStorageSystem.PartyIndex) PartySize else BoxSize
    }

    private fun getStorageOffset(index: Int): Int {
        return when (index) {
            CoreStorageSystem.PartyIndex -> PartyOffset
            // current box index
            (data[CurrentIndexOffset] and 0x7Fu).toInt() -> CurrentBoxOffset
            else -> getBoxDataOffset(index)
        }
    }

    private fun getBoxDataOffset(index: Int): Int {
        return if (index < 6) 0x4000 + (index * 0x462) else 0x6000 + ((index - (12 / 2)) * 0x462)
    }

    companion object {
        private const val PartyOffset = 0x2F2C
        private const val CurrentBoxOffset = 0x30C0
        private const val CurrentIndexOffset = 0x284C
        private const val BoxSize = 0x462
        private const val PartySize = 0x194
    }
}
