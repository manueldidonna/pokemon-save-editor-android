package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.Trainer
import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.utils.getStringFromGameBoyData
import com.manueldidonna.pk.utils.readBigEndianUShort
import com.manueldidonna.pk.core.SaveData as CoreSaveData

internal class SaveData(
    private val data: UByteArray,
    override val version: Version,
) : CoreSaveData {

    override fun toString(): String {
        return """
            RBY SaveData:
            - version: $version
            - data: ${data.contentToString()}
        """.trimIndent()
    }

    override val trainer: Trainer by lazy {
        Trainer(
            name = getStringFromGameBoyData(data, TrainerNameOffset, 11, false),
            visibleId = data.readBigEndianUShort(0x2605).toInt(),
            secretId = 0,
            gender = Trainer.Gender.Male
        )
    }

    override val pokedex: Pokedex by lazy { Pokedex(data) }

    override val bag: Bag by lazy { Bag(data) }

    override val storageSystem: StorageSystem = StorageSystem(data, version)

    /**
     * Export data and fix the checksum
     *
     * The checksum in Generation I is only 8 bits and has a single copy of it.
     *
     * 1. Initialize the checksum to 0
     * 2. For every byte from 0x2598 to 0x3522, inclusive, add its value to the checksum
     * 3. Invert the bits of the result.
     */
    override fun exportToBytes(): UByteArray {
        // don't export the internal data
        val export = data.copyOf()
        // Fix checksum
        var checksum = 0
        for (i in TrainerNameOffset until ChecksumOffset)
            checksum += export[i].toInt()
        export[ChecksumOffset] = checksum.toUByte() xor 0xFFu
        return export
    }

    companion object {
        private const val ChecksumOffset = 0x3523
        private const val TrainerNameOffset = 0x2598
    }
}
