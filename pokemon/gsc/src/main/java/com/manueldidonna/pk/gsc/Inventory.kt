package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.Inventory.Type
import com.manueldidonna.pk.core.isEmpty
import com.manueldidonna.pk.core.isMachinesType
import com.manueldidonna.pk.gsc.converter.getLocalItemId
import com.manueldidonna.pk.gsc.converter.getUniversalItemId
import com.manueldidonna.pk.core.Inventory as CoreInventory

internal class Inventory(
    private val data: UByteArray,
    private val startDataOffset: Int,
    override val type: Type,
    override val capacity: Int,
    override val supportedItemIds: List<Int>,
    override val maxQuantity: Int,
) : CoreInventory {

    override var size: Int
        get() {
            if (type.isMachinesType) {
                return getSizeFromMachinesInventory()
            }
            val size = data[startDataOffset].toInt()
            // sanity check for out-of-bounds values
            return if (size > capacity) 0 else size.coerceAtLeast(0)
        }
        private set(value) {
            // can't modify machines inventory size
            if (type.isMachinesType) return
            data[startDataOffset] = value.coerceIn(0, capacity).toUByte()
            // terminator
            val terminatorOffset = startDataOffset + 1 + if (type == Type.Keys) size else size * 2
            data[terminatorOffset] = 0xFFu
        }

    /**
     * Loop over all the inventory offsets to count not empty slots
     */
    private fun getSizeFromMachinesInventory(): Int {
        var size = 0
        for (i in 0 until capacity) {
            if (data[startDataOffset + i] > 0u) size++
        }
        return size
    }

    override fun <T> selectItem(
        index: Int,
        mapTo: (index: Int, id: Int, quantity: Int) -> T,
    ): T {
        checkItemIndex(index)
        if (index >= size) return mapTo(index, 0, 0)
        if (type.isMachinesType) {
            val machineIndex = getMachineIndex(index)
            return mapTo(
                index,
                supportedItemIds[machineIndex],
                data[startDataOffset + machineIndex].toInt()
            )
        }
        val itemIdOffset = startDataOffset + 1 + if (type == Type.Keys) index else index * 2
        val itemCount = if (type == Type.Keys) 1 else {
            data[startDataOffset + (index * 2) + 2].toInt().coerceAtMost(maxQuantity)
        }
        return mapTo(index, getUniversalItemId(data[itemIdOffset].toInt()), itemCount)
    }

    /**
     * Return the real index associated with TM/HM.
     * Throw an [IllegalStateException] if [index] is greater than [size]
     *
     * TM/HM are ordered by their id so I have to loop over all the offsets to find the right one
     */
    private fun getMachineIndex(index: Int): Int {
        var loop = -1
        for (i in 0 until capacity) {
            if (data[startDataOffset + i] > 0u) loop++
            if (loop == index) return i
        }
        throw IllegalStateException("No machines found at index $index")
    }

    override fun setItem(item: CoreInventory.Item, index: Int) {
        checkItemIndex(index)
        if (item.isEmpty && index < size) {
            deleteItemByIndex(index)
            return
        }
        require(item.id in supportedItemIds) {
            "Item Id ${item.id} is not supported"
        }
        val itemQuantity = item.quantity.coerceIn(0, maxQuantity).toUByte()
        if (type.isMachinesType) {
            // delete the previous machine at index
            deleteItemByIndex(item.index)
            val machineNumberById = supportedItemIds.indexOf(item.id)
            data[startDataOffset + machineNumberById] = itemQuantity
        } else {
            @Suppress("NAME_SHADOWING")
            val index = coerceIndexBySize(index)
            val localItemId = getLocalItemId(item.id).toUByte()
            if (type == Type.Keys) {
                data[startDataOffset + 1 + index] = localItemId
            } else {
                data[startDataOffset + (index * 2) + 1] = localItemId
                data[startDataOffset + (index * 2) + 2] = itemQuantity
            }
        }
    }

    private fun coerceIndexBySize(index: Int): Int {
        if (type.isMachinesType) return index
        if (index >= size) size++
        return index.coerceAtMost(size - 1)
    }

    private fun checkItemIndex(index: Int) {
        require(index in 0 until capacity) {
            "Index $index out of Inventory bounds [0 - $capacity]"
        }
    }

    private fun deleteItemByIndex(index: Int) {
        if (type.isMachinesType) {
            data[startDataOffset + getMachineIndex(index)] = 0u
            return
        }
        val lastIndexOffset = startDataOffset + 1 + (capacity - 1) * 2
        //shift items left of 1 position
        if (index < size - 1) {
            val destinationOffset = startDataOffset + 1 + (index) * 2
            val startShiftOffset = startDataOffset + 1 + (index + 1) * 2
            data.copyInto(data, destinationOffset, startShiftOffset, lastIndexOffset + 2)
        }
        data.fill(0u, lastIndexOffset, lastIndexOffset + 2)
        size--
    }
}
