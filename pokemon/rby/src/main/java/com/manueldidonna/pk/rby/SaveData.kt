package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.rby.converter.getGameBoyDataFromString
import com.manueldidonna.pk.rby.converter.getStringFromGameBoyData
import com.manueldidonna.pk.rby.utils.readBigEndianUShort
import com.manueldidonna.pk.rby.utils.writeBidEndianShort
import com.manueldidonna.pk.core.Inventory as CoreInventory
import com.manueldidonna.pk.core.Pokedex as CorePokedex
import com.manueldidonna.pk.core.SaveData as CoreSaveData
import com.manueldidonna.pk.core.Storage as CoreStorage

internal class SaveData(
    private val data: UByteArray,
    override val version: Version
) : CoreSaveData {

    override var trainer: Trainer
        get() = Trainer(
            // TODO: extract NAMEmAXsIZE TO A SEPARATE COnsTANT
            name = getStringFromGameBoyData(data, 0x2598, Pokemon.NameMaxSize, false),
            visibleId = data.readBigEndianUShort(0x2605).toInt(),
            secretId = 0
        )
        set(value) {
            getGameBoyDataFromString(value.name, 7, false, 11, false).copyInto(data, 0x2598)
            data.writeBidEndianShort(0x2605, value.visibleId.coerceAtMost(65535).toShort())
        }

    override val pokedex: CorePokedex by lazy { Pokedex(data) }

    override val supportedInventoryTypes: List<CoreInventory.Type> =
        listOf(CoreInventory.Type.General, CoreInventory.Type.Computer)

    override fun getInventory(type: CoreInventory.Type): CoreInventory {
        require(type in supportedInventoryTypes) {
            "Type $type is not supported by this Inventory instance"
        }
        return Inventory(type, data)
    }

    override val indices: IntRange = StorageCollection.PartyIndex until 12

    override fun getStorage(index: Int): CoreStorage {
        require(index in indices) { "Index $index is out of bounds [$indices]" }
        val dataOffset = getStorageOffset(index)
        val size = if (index.isPartyIndex) Storage.PartySize else Storage.BoxSize
        return Storage(
            data = data.copyOfRange(dataOffset, dataOffset + size),
            startOffset = 0,
            index = index,
            capacity = if (index.isPartyIndex) 6 else 20,
            version = version
        )
    }

    override fun getMutableStorage(index: Int): MutableStorage {
        require(index in indices) { "Index $index is out of bounds [$indices]" }
        val dataOffset = getStorageOffset(index)
        return Storage(data, dataOffset, index, if (index.isPartyIndex) 6 else 20, version)
    }

    private fun getStorageOffset(index: Int): Int {
        return when (index) {
            StorageCollection.PartyIndex -> PartyOffset
            // current box index
            (data[CurrentIndexOffset] and 0x7Fu).toInt() -> CurrentBoxOffset
            else -> getBoxDataOffset(index)
        }
    }

    private fun getBoxDataOffset(index: Int): Int {
        return if (index < 6) 0x4000 + (index * 0x462) else 0x6000 + ((index - (12 / 2)) * 0x462)
    }

    /**
     * Export data and fix the checksum
     *
     * The checksum in Generation I is only 8 bits and has a single copy of it.
     * Checksum offset is 0x3523
     *
     * 1. Initialize the checksum to 0
     * 2. For every byte from 0x2598 to 0x3522, inclusive, add its value to the checksum
     * 3. Invert the bits of the result.
     */
    override fun exportToBytes(): UByteArray {
        // don't export the internal data
        val export = data.copyOf()
        // Fix checksum
        var checksum: Int = 0
        for (i in 0x2598 until 0x3523)
            checksum += export[i].toInt()
        export[0x3523] = checksum.toUByte() xor 0xFFu
        return export
    }

    companion object {
        private const val PartyOffset = 0x2F2C
        private const val CurrentBoxOffset = 0x30C0
        private const val CurrentIndexOffset = 0x284C
    }
}
