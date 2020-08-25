package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.Inventory.Type
import com.manueldidonna.pk.core.isEmpty
import com.manueldidonna.pk.gsc.converter.getLocalItemId
import com.manueldidonna.pk.gsc.converter.getUniversalItemId
import com.manueldidonna.pk.core.Inventory as CoreInventory

internal class ItemsInventory(
    override val type: Type,
    private val data: UByteArray,
    override val capacity: Int,
    private val startOffset: Int,
    override val supportedItemIds: List<Int>,
    override val maxQuantity: Int,
) : CoreInventory {

    override var size: Int
        get() {
            val size = data[startOffset].toInt()
            // sanity check for out-of-bounds values
            return if (size > capacity) 0 else size
        }
        private set(value) {
            data[startOffset] = value.coerceIn(0, capacity).toUByte()
            // terminator
            data[startOffset + 1 + (size * 2)] = 0xFFu
        }

    override fun <T> selectItem(index: Int, mapTo: (index: Int, id: Int, quantity: Int) -> T): T {
        require(index in 0 until capacity) {
            "Index $index out of Inventory bounds [0 - $capacity]"
        }
        if (index >= size) return mapTo(index, 0, 0)
        return mapTo(
            index,
            getUniversalItemId(data[startOffset + 1 + (index * 2)].toInt()),
            data[startOffset + 1 + (index * 2) + 1].toInt().coerceAtMost(maxQuantity)
        )
    }


    override fun setItem(item: CoreInventory.Item, index: Int) {
        require(index in 0 until capacity) {
            "Index $index out of Inventory bounds [0 - $capacity]"
        }
        if (item.isEmpty) {
            deleteItem(index)
        } else {
            require(item.id in supportedItemIds) { "Item Id ${item.id} is not supported" }
            if (index >= size) size++
            val offset = startOffset + (index.coerceAtMost(size - 1) * 2) + 1
            data[offset] = getLocalItemId(item.id).toUByte()
            data[offset + 1] = item.quantity.coerceAtMost(maxQuantity).toUByte()
        }
    }

    private fun deleteItem(index: Int) {
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
}

internal class MachinesInventory(
    override val type: Type,
    private val data: UByteArray,
    override val capacity: Int,
    private val startOffset: Int,
    override val supportedItemIds: List<Int>,
    override val maxQuantity: Int,
) : CoreInventory {

    override val size: Int
        get() {
            var size = 0
            for (i in 0 until capacity) {
                if (data[startOffset + i] > 0u) size++
            }
            return size
        }

    /**
     * Machines are stored by their position in the items list.
     * TM1 is at index 0, TM2 at 1, etc...
     */
    override fun <T> selectItem(index: Int, mapTo: (index: Int, id: Int, quantity: Int) -> T): T {
        require(index in 0 until capacity) {
            "Index $index out of Inventory bounds [0 - $capacity]"
        }
        if (index >= size) return mapTo(index, 0, 0)
        val realIndex = findMachineIndex(index).coerceAtLeast(0)
        return mapTo(index, supportedItemIds[realIndex], data[startOffset + realIndex].toInt())
    }

    override fun setItem(item: com.manueldidonna.pk.core.Inventory.Item, index: Int) {
        require(index in 0 until capacity) {
            "Index $index out of Inventory bounds [0 - $capacity]"
        }
        if (item.isEmpty) {
            deleteMachine(index)
        } else {
            require(item.id in supportedItemIds) { "Item Id ${item.id} is not supported" }
            deleteMachine(item.index)
            val machineIndex = supportedItemIds.indexOf(item.id)
            data[startOffset + machineIndex] = item.quantity.coerceIn(0, maxQuantity).toUByte()
        }
    }

    private fun deleteMachine(index: Int) {
        val realIndex = findMachineIndex(index)
        if (realIndex != -1) {
            data[startOffset + realIndex] = 0u
        }
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
}
