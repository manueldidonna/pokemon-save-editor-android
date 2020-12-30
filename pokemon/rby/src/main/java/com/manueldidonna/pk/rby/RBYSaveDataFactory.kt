package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.core.Version

object RBYSaveDataFactory : SaveData.Factory {
    override fun create(data: UByteArray): SaveData? {
        return when {
            data.size != 0x8000 && data.size != 0x802C -> null
            data.isInternationalSaveData() -> SaveData(data, data.getGameVersion())
            // TODO: DETECT jap saves
            else -> null
        }
    }

    private fun UByteArray.isInternationalSaveData(): Boolean {
        return isListValid(this, 0x2F2C, 20) && isListValid(this, 0x30C0, 20)
    }

    private fun isListValid(data: UByteArray, offset: Int, listCount: Int): Boolean {
        val numEntries = data[offset].toInt()
        return numEntries <= listCount && data[offset + 1 + numEntries] == 0xff.toUByte()
    }

    private const val PlayerStarterOffset = 0x29C3

    private fun UByteArray.getGameVersion(): Version {
        val isPikachuStarter = this[PlayerStarterOffset] == 0x54.toUByte()
        return if (isPikachuStarter) Version.Yellow else Version.Red
    }
}
