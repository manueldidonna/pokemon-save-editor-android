package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.core.Inventory.Type
import com.manueldidonna.pk.core.Item
import com.manueldidonna.pk.core.isEmpty
import com.manueldidonna.pk.core.isMachinesType
import com.manueldidonna.pk.gsc.converter.getLocalItemId
import com.manueldidonna.pk.gsc.converter.getUniversalItemId
import com.manueldidonna.pk.core.Inventory as CoreInventory

internal class Inventory(
    private val data: UByteArray,
    private val startOffset: Int,
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
            val size = data[startOffset].toInt()
            // sanity check for out-of-bounds values
            return if (size > capacity) 0 else size.coerceAtLeast(0)
        }
        private set(value) {
            // can't modify machines inventory size
            if (type.isMachinesType) return
            data[startOffset] = value.coerceIn(0, capacity).toUByte()
            // terminator
            val terminatorOffset = startOffset + 1 + if (type == Type.Keys) size else size * 2
            data[terminatorOffset] = 0xFFu
        }

    /**
     * Loop over all the inventory offsets to count not empty slots
     */
    private fun getSizeFromMachinesInventory(): Int {
        var size = 0
        for (i in 0 until capacity) {
            if (data[startOffset + i] > 0u) size++
        }
        return size
    }

    override fun <I> selectItem(index: Int, mapper: CoreInventory.ItemMapper<I>): I {
        checkItemIndex(index)
        if (index >= size) return mapper.mapTo(0, 0)
        if (type.isMachinesType) {
            val machineIndex = getMachineIndex(index)
            return mapper.mapTo(
                id = supportedItemIds[machineIndex],
                quantity = data[startOffset + machineIndex].toInt()
            )
        }
        val itemIdOffset = startOffset + 1 + if (type == Type.Keys) index else index * 2
        val itemCount = if (type == Type.Keys) 1 else {
            data[startOffset + (index * 2) + 2].toInt().coerceAtMost(maxQuantity)
        }
        return mapper.mapTo(
            id = getUniversalItemId(data[itemIdOffset].toInt()),
            quantity = itemCount
        )
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
            if (data[startOffset + i] > 0u) loop++
            if (loop == index) return i
        }
        throw IllegalStateException("No machines found at index $index")
    }

    override fun setItem(index: Int, item: Item) {
        checkItemIndex(index)
        require(!item.isEmpty) { "Item cannot be empty" }
        require(item.id in supportedItemIds) {
            "Item Id ${item.id} is not supported"
        }
        val itemQuantity = item.quantity.coerceIn(0, maxQuantity).toUByte()
        if (type.isMachinesType) {
            // delete the previous machine at index
            removeItemAt(index)
            val machineNumberById = supportedItemIds.indexOf(item.id)
            data[startOffset + machineNumberById] = itemQuantity
        } else {
            @Suppress("NAME_SHADOWING")
            val index = coerceIndexBySize(index)
            val localItemId = getLocalItemId(item.id).toUByte()
            if (type == Type.Keys) {
                data[startOffset + 1 + index] = localItemId
            } else {
                data[startOffset + (index * 2) + 1] = localItemId
                data[startOffset + (index * 2) + 2] = itemQuantity
            }
        }
    }

    override fun removeItemAt(index: Int) {
        checkItemIndex(index)
        if (type.isMachinesType) {
            data[startOffset + getMachineIndex(index)] = 0u
            return
        }
        if (index > size) return
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

    private fun coerceIndexBySize(index: Int): Int {
        if (type.isMachinesType) return index
        if (index >= size) size++
        return index.coerceAtMost(size - 1)
    }

    private fun checkItemIndex(index: Int) {
        require(index in 0 until capacity) {
            "Index $index out of bounds [0..${capacity - 1}]"
        }
    }
}
