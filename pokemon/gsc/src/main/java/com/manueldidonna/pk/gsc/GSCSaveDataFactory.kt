package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.core.Version

// TODO: DETECT jap and kor saves
object GSCSaveDataFactory : SaveData.Factory {

    private const val SizeRawU = 0x8000
    private const val SizeVirtualU = 0x8010
    private const val SizeBatU = 0x802C
    private const val SizeEmulatorU = 0x8030

    override fun create(data: UByteArray): SaveData? {
        val supportedSizes = listOf(SizeBatU, SizeEmulatorU, SizeRawU, SizeVirtualU)
        return when {
            !supportedSizes.contains(data.size) -> null
            // International Gold/Silver
            isListValid(data, 0x288A, 20) && isListValid(data, 0x2D6C, 20) -> {
                // TODO: detect version. Default to Silver
                SaveData(Version.Silver, data)
            }
            // International Crystal
            isListValid(data, 0x2865, 20) && isListValid(data, 0x2D10, 20) -> {
                SaveData(Version.Crystal, data)
            }
            else -> null
        }
    }

    private fun isListValid(data: UByteArray, offset: Int, listCount: Int): Boolean {
        val numEntries = data[offset].toInt()
        return numEntries <= listCount && data[offset + 1 + numEntries] == 0xff.toUByte()
    }
}