package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.MutableStorage
import com.manueldidonna.pk.core.StorageIndex
import com.manueldidonna.pk.core.Trainer
import com.manueldidonna.pk.core.isParty
import com.manueldidonna.pk.rby.converter.getGameBoyDataFromString
import com.manueldidonna.pk.rby.converter.getStringFromGameBoyData
import com.manueldidonna.pk.rby.utils.*
import com.manueldidonna.pk.core.Pokedex as CorePokedex
import com.manueldidonna.pk.core.SaveData as CoreSaveData
import com.manueldidonna.pk.core.Storage as CoreStorage

internal class SaveData(private val data: UByteArray) : CoreSaveData {

    companion object {
        private const val CurrentBoxOffset = 0x30C0
    }

    override var trainer: Trainer
        get() = Trainer(
            name = getStringFromGameBoyData(data, 0x2598, NameSize, false),
            visibleId = data.readBigEndianUShort(0x2605).toUInt(),
            secretId = 0u
        )
        set(value) {
            getGameBoyDataFromString(value.name, 7, false, 11, false).copyInto(data, 0x2598)
            data.writeBidEndianShort(0x2605, value.visibleId.toShort())
        }

    override val boxCounts: Int = 12

    override var currentBoxIndex: Int
        get() = (data[0x284C] and 0x7Fu).toInt()
        set(value) {
            require(value in 0..11) { " Box index must be 0-11 but is $value" }
            if (value == currentBoxIndex) return
            val oldValue = currentBoxIndex
            val newBoxOffset = getBoxDataOffset(value)
            data[0x284C] = (data[0x284C] and 0x80u) or (value and 0x7F).toUByte()
            val oldBoxOffset = getBoxDataOffset(oldValue)
            data.copyInto(data, oldBoxOffset, CurrentBoxOffset, CurrentBoxOffset + BoxSize)
            data.copyInto(data, CurrentBoxOffset, newBoxOffset, newBoxOffset + BoxSize)
        }

    override fun getStorage(index: StorageIndex): CoreStorage {
        val dataOffset = if (index.isParty) 0x2F2C else getBoxDataOffset(index.value)
        val size = if (index.isParty) PartySize else BoxSize
        val pokemonCounts = if (index.isParty) 6 else 20
        return Storage(data.copyOfRange(dataOffset, dataOffset + size), 0, index, pokemonCounts)
    }

    override fun getMutableStorage(index: StorageIndex): MutableStorage {
        val dataOffset = if (index.isParty) 0x2F2C else getBoxDataOffset(index.value)
        return Storage(data, dataOffset, index, if (index.isParty) 6 else 20)
    }

    private fun getBoxDataOffset(index: Int): Int {
        require(index in 0..11) { " Box index must be 0-11" }
        return if (index == currentBoxIndex) CurrentBoxOffset else {
            if (index < 6) 0x4000 + (index * 0x462) else 0x6000 + ((index - (12 / 2)) * 0x462)
        }
    }

    override fun getPokedex(): CorePokedex {
        return Pokedex(data)
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
        // Fix checksum
        var checksum: Int = 0
        for (i in 0x2598 until 0x3523)
            checksum += data[i].toInt()
        data[0x3523] = checksum.toUByte() xor 0xFFu
        return data
    }
}