package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.SaveData

object RBYSaveDataFactory : SaveData.Factory {

    private fun isListValid(data: UByteArray, offset: Int, listCount: Int): Boolean {
        val numEntries = data[offset].toInt()
        return numEntries <= listCount && data[offset + 1 + numEntries] == 0xff.toUByte()
    }

    override fun createSaveData(data: UByteArray): SaveData? {
        return when {
            data.size != 0x8000 && data.size != 0x802C -> null
            isListValid(data, 0x2F2C, 20) && isListValid(data, 0x30C0, 20) -> SaveData(data)
            // TODO: support japanese save data
            //  isListValid(data, 0x2ED5, 30) && isListValid(data, 0x302D, 30) -> SaveData(data)
            else -> null
        }
    }
}
