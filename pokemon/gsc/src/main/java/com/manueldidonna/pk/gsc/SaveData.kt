package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.utils.setLittleEndianShort
import com.manueldidonna.pk.core.Inventory as CoreInventory
import com.manueldidonna.pk.core.SaveData as CoreSaveData

internal class SaveData(
    override val version: Version,
    private val data: UByteArray
) : CoreSaveData {

    override val trainer: Trainer
        get() = TODO("Not yet implemented")

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

    override val indices: IntRange = StorageCollection.PartyIndex until 14

    override fun getStorage(index: Int): Storage {
        TODO("Not yet implemented")
    }

    override fun getMutableStorage(index: Int): MutableStorage {
        TODO("Not yet implemented")
    }

    /**
     * Data in Generation II is stored in the save file twice.
     * Checksums are performed on both copies and stored in the data as little-endian.
     * The checksums are simply the 16-bit sum of byte values in a specific byte regions.
     */
    override fun exportToBytes(): UByteArray {
        // don't export the internal data
        val export = data.copyOf()
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