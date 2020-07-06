package com.manueldidonna.pk.gsc

import com.manueldidonna.pk.gsc.converter.getLocalItemId
import com.manueldidonna.pk.gsc.converter.getUniversalItemId
import com.manueldidonna.pk.core.Inventory as CoreInventory

internal class Inventory(
    override val type: CoreInventory.Type,
    private val data: UByteArray
) : CoreInventory {

    override val maxAllowedQuantity: Int = TODO()

    override val supportedItemIds: List<Int> = TODO()

    override val capacity: Int = TODO()

    private val startOffset: Int = TODO()



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

    // gen 2 inventory store the count before the id
    override fun <T> selectItem(index: Int, mapTo: (index: Int, id: Int, quantity: Int) -> T): T {
        require(index in 0 until capacity) {
            "Index ($index) out of Inventory bounds [0-$capacity]"
        }
        if (index >= size)
            return mapTo(index, 0, 0)
        return mapTo(
            index,
            getUniversalItemId(data[startOffset + 1 + (index * 2) + 1].toInt()),
            data[startOffset + 1 + (index * 2)].toInt()
        )
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
            if (index >= size) size++
            val offset = startOffset + (index.coerceAtMost(size - 1) * 2) + 1
            data[offset] = getLocalItemId(item.id).toUByte()
            data[offset + 1] = item.quantity.coerceAtMost(maxAllowedQuantity).toUByte()
        }
    }

    private fun setMachine(item: CoreInventory.Item) {
        val index = supportedItemIds.indexOf(item.id)
        data[startOffset + index] = item.quantity.coerceAtMost(1).toUByte()
    }

    private fun deleteItem(index: Int) {
        val lastIndexOffset = startOffset + 1 + (capacity - 1) * 2
        //shift items left of 1 position
        if (index < capacity - 1) {
            val destinationOffset = startOffset + 1 + (index) * 2
            val startShiftOffset = startOffset + 1 + (index + 1) * 2
            data.copyInto(data, destinationOffset, startShiftOffset, lastIndexOffset + 2)
        }
        data.fill(0u, lastIndexOffset, lastIndexOffset + 2)
        size--
    }

    companion object {
        private const val GeneralOffset = 0x25C9
        private const val ComputerOffset = 0x27E6
    }
}
