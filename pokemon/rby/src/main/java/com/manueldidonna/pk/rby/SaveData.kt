package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.MutableBox
import com.manueldidonna.pk.core.SaveData as CoreSaveData
import com.manueldidonna.pk.core.Box as CoreBox

internal class SaveData(private val data: UByteArray) : CoreSaveData {

    companion object {
        private const val CurrentBoxOffset = 0x30C0
    }

    override val boxCounts: Int = 12

    override var currentBoxIndex: Int
        get() = (data[0x284C] and 0x7Fu).toInt()
        set(value) {
            require(value in 0..11) { " Box index must be 0-11" }
            if (value == currentBoxIndex) return
            val oldValue = currentBoxIndex
            val newBoxOffset = getBoxDataOffset(value)
            data[0x284C] = (data[0x284C] and 0x80u) or (value and 0x7F).toUByte()
            val oldBoxOffset = getBoxDataOffset(oldValue)
            data.copyInto(data, oldBoxOffset, CurrentBoxOffset, CurrentBoxOffset + BoxSize)
            data.copyInto(data, CurrentBoxOffset, newBoxOffset, newBoxOffset + BoxSize)
        }

    override fun getBox(index: Int): CoreBox {
        val boxDataOffset = getBoxDataOffset(index)
        return Box(data.copyOfRange(boxDataOffset, boxDataOffset + BoxSize), 0, index)
    }

    override fun getMutableBox(index: Int): MutableBox {
        return Box(data, getBoxDataOffset(index), index)
    }

    private fun getBoxDataOffset(index: Int): Int {
        require(index in 0..11) { " Box index must be 0-11" }
        return if (index == currentBoxIndex) CurrentBoxOffset else {
            if (index < 6) 0x4000 + (index * 0x462) else 0x6000 + ((index - (12 / 2)) * 0x462)
        }
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