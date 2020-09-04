package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.core.Version

// TODO: DETECT jap saves
object RBYSaveDataFactory : SaveData.Factory {

    private const val PlayerStarterOffset = 0x29C3

    override fun create(data: UByteArray): SaveData? {
        return when {
            data.size != 0x8000 && data.size != 0x802C -> null
            isListValid(data, 0x2F2C, 20) && isListValid(data, 0x30C0, 20) -> {
                val isPikachuStarter = data[PlayerStarterOffset] == 0x54.toUByte()
                val version = if (isPikachuStarter) Version.Yellow else Version.Red
                SaveData(data, version)
            }
            else -> null
        }
    }

    private fun isListValid(data: UByteArray, offset: Int, listCount: Int): Boolean {
        val numEntries = data[offset].toInt()
        return numEntries <= listCount && data[offset + 1 + numEntries] == 0xff.toUByte()
    }
}
