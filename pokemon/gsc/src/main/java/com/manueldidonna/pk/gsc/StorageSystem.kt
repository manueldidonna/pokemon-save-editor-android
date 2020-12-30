package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.Storage
import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.utils.copyIntoFor
import com.manueldidonna.pk.utils.getStringFromGameBoyData
import com.manueldidonna.pk.core.StorageSystem as CoreStorageSystem

internal class StorageSystem(
    private val data: UByteArray,
    private val version: Version,
) : CoreStorageSystem {

    override val storageIndices = CoreStorageSystem.PartyIndex until 14

    private val offsets: Offsets =
        if (version == Version.Crystal) Offsets.InternationalCrystal
        else Offsets.InternationalGoldSilver

    override fun set(index: Int, storage: Storage) {
        checkStorageIndex(index)
        val import = storage.exportToBytes()
        require(import.size == getStorageSize(index)) {
            "Incompatible Storage Data Size: ${storage.size}"
        }
        import.copyInto(data, getStorageOffset(index))
    }

    override fun get(index: Int): Storage {
        checkStorageIndex(index)
        return Storage(
            data = getStorageData(index),
            version = version,
            storageIndex = index,
            capacity = if (index == CoreStorageSystem.PartyIndex) 6 else 20,
            name = getStorageName(index)
        )
    }

    private fun checkStorageIndex(index: Int) {
        require(index in storageIndices) {
            "Index $index is out of bounds [$storageIndices]"
        }
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
            CoreStorageSystem.PartyIndex -> offsets.party
            getCurrentBoxIndex() -> offsets.currentBox
            else -> BoxOffsets[index]
        }
    }

    private fun getCurrentBoxIndex(): Int {
        return (data[offsets.currentBoxIndex] and 0x7Fu).toInt()
    }

    private fun getStorageName(index: Int): String {
        if (index == CoreStorageSystem.PartyIndex) return "PARTY"
        val offset = offsets.boxNames + (9 * index)
        return getStringFromGameBoyData(data, offset, 9, false)
    }

    internal fun exportCurrentStorage(into: UByteArray) {
        val currentIndex = getCurrentBoxIndex()
        val currentOffset = getStorageOffset(currentIndex)
        data.copyIntoFor(into, BoxOffsets[currentIndex], currentOffset, length = BoxSize)
    }

    companion object {
        private const val PartySize = 428
        private const val BoxSize = 1102
        private val BoxOffsets = listOf(
            0x4000, 0x4450, 0x48a0, 0x4cf0, 0x5140, 0x5590, 0x59e0,
            0x6000, 0x6450, 0x68a0, 0x6cf0, 0x7140, 0x7590, 0x79e0
        )
    }

    private sealed class Offsets {
        abstract val currentBoxIndex: Int
        abstract val currentBox: Int
        abstract val party: Int
        abstract val boxNames: Int

        object InternationalCrystal : Offsets() {
            override val currentBoxIndex = 0x2700
            override val currentBox = 0x2D10
            override val party = 0x2865
            override val boxNames = 0x2703
        }

        object InternationalGoldSilver : Offsets() {
            override val currentBoxIndex = 0x2724
            override val currentBox = 0x2D6C
            override val party = 0x288A
            override val boxNames = 0x2727
        }
    }
}
