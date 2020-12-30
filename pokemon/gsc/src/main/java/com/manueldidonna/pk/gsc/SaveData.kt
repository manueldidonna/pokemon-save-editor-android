package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.Bag
import com.manueldidonna.pk.core.Trainer
import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.utils.getStringFromGameBoyData
import com.manueldidonna.pk.utils.readBigEndianUShort
import com.manueldidonna.pk.utils.setLittleEndianShort
import com.manueldidonna.pk.core.SaveData as CoreSaveData

internal class SaveData(
    override val version: Version,
    private val data: UByteArray,
) : CoreSaveData {

    override fun toString(): String {
        return "GSC SaveData ver. $version"
    }

    override val trainer: Trainer by lazy {
        Trainer(
            name = getStringFromGameBoyData(data, 0x200B, 11, false),
            visibleId = data.readBigEndianUShort(0x2009).toInt(),
            secretId = 0,
            gender = getTrainerGender()
        )
    }

    private fun getTrainerGender(): Trainer.Gender {
        // Trainer gender is exclusive to Crystal
        if (version != Version.Crystal) return Trainer.Gender.Male
        return if (data[0x3E3D].toInt() == 0) Trainer.Gender.Male else Trainer.Gender.Female
    }

    override val pokedex: Pokedex by lazy { Pokedex(data, version) }

    override val bag: Bag by lazy { Bag(data, version) }

    override val storageSystem = StorageSystem(data, version)

    /**
     * Data in Generation II is stored in the save file twice.
     * Checksums are performed on both copies and stored in the data as little-endian.
     * The checksums are simply the 16-bit sum of byte values in a specific byte regions.
     */
    override fun exportToBytes(): UByteArray {
        // don't export the internal data
        val export = data.copyOf()

        // copy current storage
        storageSystem.exportCurrentStorage(export)

        // get checksum
        val checksumEndOffset = if (version == Version.Crystal) 0x2B82 else 0x2D68
        var checksum: UShort = 0u
        for (i in 0x2009..checksumEndOffset) {
            checksum = (checksum + export[i]).toUShort()
        }

        when (version) {
            Version.Crystal -> {
                // set checksum
                export.setLittleEndianShort(0x2D0D, checksum.toShort())
                export.setLittleEndianShort(0x1F0D, checksum.toShort())
                // copy data into the second block
                export.copyInto(export, 0x1209, 0x2009, 0x2B82)
            }
            else -> {
                // set checksum
                export.setLittleEndianShort(0x2D69, checksum.toShort())
                export.setLittleEndianShort(0x7E6D, checksum.toShort())
                // copy data into the second block
                export.copyInto(export, 0x15C7, 0x2009, 0x222E)
                export.copyInto(export, 0x3D96, 0x222F, 0x23D8)
                export.copyInto(export, 0x0C6B, 0x23D9, 0x2855)
                export.copyInto(export, 0x7E39, 0x2856, 0x2889)
                export.copyInto(export, 0x10E8, 0x288A, 0x2D68)
            }
        }
        return export
    }
}