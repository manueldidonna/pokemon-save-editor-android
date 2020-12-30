package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.pk.core.Version

object GSCSaveDataFactory : SaveData.Factory {
    override fun create(data: UByteArray): SaveData? {
        return when {
            !data.isSizeSupported() -> null
            // TODO: detect version. Default to Silver
            data.isInternationalGoldSilver() -> SaveData(Version.Silver, data)
            data.isInternationalCrystal() -> SaveData(Version.Crystal, data)
            // TODO: detect jap and kor saves
            else -> null
        }
    }

    private const val SizeRawU = 0x8000
    private const val SizeVirtualU = 0x8010
    private const val SizeBatU = 0x802C
    private const val SizeEmulatorU = 0x8030

    private fun UByteArray.isSizeSupported(): Boolean {
        val supportedSizes = listOf(SizeBatU, SizeEmulatorU, SizeRawU, SizeVirtualU)
        return supportedSizes.contains(size)
    }

    private fun UByteArray.isInternationalGoldSilver(): Boolean {
        return isListValid(this, 0x288A, 20) && isListValid(this, 0x2D6C, 20)
    }

    private fun UByteArray.isInternationalCrystal(): Boolean {
        return isListValid(this, 0x2865, 20) && isListValid(this, 0x2D10, 20)
    }

    private fun isListValid(data: UByteArray, offset: Int, listCount: Int): Boolean {
        val numEntries = data[offset].toInt()
        return numEntries <= listCount && data[offset + 1 + numEntries] == 0xff.toUByte()
    }
}
