package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.Inventory.Type
import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.gsc.converter.getIdsByType
import com.manueldidonna.pk.gsc.converter.getLocalItemId
import com.manueldidonna.pk.gsc.converter.getUniversalItemId
import com.manueldidonna.pk.core.Inventory as CoreInventory

internal class Inventory private constructor(
    override val type: Type,
    private val data: UByteArray,
    override val capacity: Int,
    private val startOffset: Int
) : CoreInventory {

    /**
     * TM/HM Pocket. In gen 2 TMs can't be duplicated. Machines are ordered by their id
     */
    private val isMachinesPocket = type == Type.TechnicalMachines || type == Type.HiddenMachines

    override val maxQuantity = if (type == Type.HiddenMachines || type == Type.Keys) 1 else 99

    override val supportedItemIds: List<Int> = getIdsByType(type)

    override var size: Int
        get() {
            if (isMachinesPocket) {
                var size = 0
                for (i in 0 until capacity) {
                    if (data[startOffset + i] > 0u) size++
                }
                return size
            }
            val size = data[startOffset].toInt()
            // sanity check for out-of-bounds values
            return if (size > capacity) 0 else size
        }
        private set(value) {
            require(!isMachinesPocket)
            data[startOffset] = value.coerceIn(0, capacity).toUByte()
            // terminator
            data[startOffset + 1 + (size * 2)] = 0xFFu
        }

    override fun <T> selectItem(index: Int, mapTo: (index: Int, id: Int, quantity: Int) -> T): T {
        require(index in 0 until capacity) {
            "Index ($index) out of Inventory bounds [0-$capacity]"
        }
        return when {
            index >= size -> mapTo(index, 0, 0)
            isMachinesPocket -> selectMachine(index, mapTo)
            else -> mapTo(
                index,
                getUniversalItemId(data[startOffset + 1 + (index * 2)].toInt()),
                data[startOffset + 1 + (index * 2) + 1].toInt().coerceAtMost(maxQuantity)
            )
        }
    }

    /**
     * Select HM/TM. Machines are store by their index.
     * TM Pocket has 50 slots for TM and 7 slots for HM.
     * Machines are ordered by their index. TM1 is at index 0, TM2 at 1, etc...
     */
    private fun <T> selectMachine(index: Int, mapTo: (index: Int, id: Int, quantity: Int) -> T): T {
        val realIndex = findMachineIndex(index).coerceAtLeast(0)
        return mapTo(index, supportedItemIds[realIndex], data[startOffset + realIndex].toInt())
    }

    override fun setItem(item: CoreInventory.Item, index: Int) {
        require(index in 0 until capacity) {
            "Index ($index) out of Inventory bounds [0-$capacity]"
        }
        if (item.id == 0 || item.quantity == 0) {
            deleteItem(index)
        } else {
            require(item.id in supportedItemIds) {
                "Item id (${item.id}) is not supported"
            }
            if (isMachinesPocket) {
                setMachine(item)
            } else {
                if (index >= size) size++
                val offset = startOffset + (index.coerceAtMost(size - 1) * 2) + 1
                data[offset] = getLocalItemId(item.id).toUByte()
                data[offset + 1] = item.quantity.coerceAtMost(maxQuantity).toUByte()
            }
        }
    }

    private fun setMachine(item: CoreInventory.Item) {
        deleteMachine(item.index)
        val index = supportedItemIds.indexOf(item.id)
        data[startOffset + index] = item.quantity.coerceIn(0, maxQuantity).toUByte()
    }

    private fun deleteMachine(index: Int) {
        val realIndex = findMachineIndex(index)
        if (realIndex != -1) {
            data[startOffset + realIndex] = 0u
        }
    }

    private fun deleteItem(index: Int) {
        if (isMachinesPocket) {
            deleteMachine(index)
            return
        }
        val lastIndexOffset = startOffset + 1 + (capacity - 1) * 2
        //shift items left of 1 position
        if (index < size - 1) {
            val destinationOffset = startOffset + 1 + (index) * 2
            val startShiftOffset = startOffset + 1 + (index + 1) * 2
            data.copyInto(data, destinationOffset, startShiftOffset, lastIndexOffset + 2)
        }
        data.fill(0u, lastIndexOffset, lastIndexOffset + 2)
        size--
    }

    /**
     * Return the real index associated with TM/HM or -1
     *
     * TM/HM are ordered by their id so I have to loop over all the offsets to find the right one
     */
    private fun findMachineIndex(index: Int): Int {
        var loop = -1
        for (i in 0 until capacity) {
            if (data[startOffset + i] > 0u) loop++
            if (loop == index) {
                return i
            }
        }
        return -1
    }

    companion object {
        internal fun newInstance(data: UByteArray, version: Version, type: Type): Inventory {
            val startOffset: Int
            val capacity: Int
            when (type) {
                Type.Keys -> {
                    startOffset = if (version == Version.Crystal) 0x244A else 0x2449
                    capacity = 26
                }
                Type.Computer -> {
                    startOffset = if (version == Version.Crystal) 0x247F else 0x247E
                    capacity = 50
                }
                Type.Balls -> {
                    startOffset = if (version == Version.Crystal) 0x2465 else 0x2464
                    capacity = 12
                }
                Type.General -> {
                    startOffset = if (version == Version.Crystal) 0x2420 else 0x241F
                    capacity = 20
                }
                Type.HiddenMachines -> {
                    startOffset = (if (version == Version.Crystal) 0x23E7 else 0x23E6) + 0x32
                    capacity = 7
                }
                Type.TechnicalMachines -> {
                    startOffset = if (version == Version.Crystal) 0x23E7 else 0x23E6
                    capacity = 50
                }
                else -> throw IllegalArgumentException("Type $type is not supported")
            }
            return Inventory(type, data, capacity, startOffset)
        }
    }
}
