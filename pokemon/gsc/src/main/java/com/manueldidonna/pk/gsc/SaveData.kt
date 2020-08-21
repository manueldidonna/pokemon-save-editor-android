package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.Storage
import com.manueldidonna.pk.core.Trainer
import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.utils.getStringFromGameBoyData
import com.manueldidonna.pk.utils.readBigEndianUShort
import com.manueldidonna.pk.utils.setLittleEndianShort
import com.manueldidonna.pk.core.Inventory as CoreInventory
import com.manueldidonna.pk.core.SaveData as CoreSaveData

internal class SaveData(
    override val version: Version,
    private val data: UByteArray,
) : CoreSaveData {

    private val storageSystem = StorageSystem(data, version)

    override fun hashCode(): Int {
        return data.contentHashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (this.version != (other as SaveData).version) return false
        return this.exportToBytes().contentEquals(other.exportToBytes())
    }

    override fun toString(): String {
        return """
            GSC SaveData:
            - version: $version
            - data: ${data.contentToString()}
        """.trimIndent()
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

    override val supportedInventoryTypes: List<CoreInventory.Type> = listOf(
        CoreInventory.Type.General,
        CoreInventory.Type.Balls,
        CoreInventory.Type.TechnicalMachines,
        CoreInventory.Type.HiddenMachines,
        CoreInventory.Type.Keys,
        CoreInventory.Type.Computer
    )

    override fun getInventory(type: CoreInventory.Type): CoreInventory {
        require(type in supportedInventoryTypes) { "Type $type is not supported" }
        return Inventory.newInstance(data, version, type)
    }

    override val storageIndices: IntRange = storageSystem.storageIndices

    override fun get(index: Int): Storage = storageSystem[index]

    override fun set(index: Int, storage: Storage) = storageSystem.set(index, storage)

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