package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.Item
import com.manueldidonna.pk.core.isEmpty
import com.manueldidonna.pk.rby.converter.SupportedItemIds
import com.manueldidonna.pk.rby.converter.getGameBoyItemId
import com.manueldidonna.pk.rby.converter.getUniversalItemId
import com.manueldidonna.pk.core.Inventory as CoreInventory

internal class Inventory(
    override val type: CoreInventory.Type,
    private val data: UByteArray,
    override val capacity: Int,
    private val startOffset: Int,
) : CoreInventory {

    override val maxQuantity = 99

    override val supportedItemIds = SupportedItemIds

    override var size: Int
        get() {
            val size = data[startOffset].toInt()
            // uninitialized yellow (0xFF), sanity check for out-of-bounds values
            return if (size > capacity) 0 else size
        }
        private set(value) {
            data[startOffset] = value.coerceIn(0, capacity).toUByte()
            // end byte
            data[startOffset + 1 + size * 2] = 0xFFu
        }

    override fun <I> selectItem(index: Int, mapper: CoreInventory.ItemMapper<I>): I {
        checkItemIndex(index)
        if (index >= size)
            return mapper.mapTo(0, 0)
        return mapper.mapTo(
            id = getUniversalItemId(data[startOffset + 1 + index * 2].toInt()),
            quantity = data[startOffset + 2 + index * 2].toInt()
        )
    }


    override fun setItem(index: Int, item: Item) {
        checkItemIndex(index)
        require(!item.isEmpty) {
            "Cannot set an empty item"
        }
        require(item.id in supportedItemIds) {
            "Item Id ${item.id} is not supported"
        }
        // increase size if needed
        if (index >= size) size++
        val itemIdOffset = startOffset + index.coerceAtMost(size - 1) * 2 + 1
        data[itemIdOffset] = getGameBoyItemId(item.id).toUByte()
        data[itemIdOffset + 1] = item.quantity.coerceAtMost(maxQuantity).toUByte()
    }

    override fun removeItemAt(index: Int) {
        checkItemIndex(index)
        if(index > size) return
        val lastIndexOffset = startOffset + 1 + (capacity - 1) * 2
        //shift items left of 1 position
        if (index < size - 1) {
            val destinationOffset = startOffset + 1 + index * 2
            val startShiftOffset = startOffset + 1 + (index + 1) * 2
            data.copyInto(data, destinationOffset, startShiftOffset, lastIndexOffset + 2)
        }
        data.fill(0u, lastIndexOffset, lastIndexOffset + 2)
        size--
    }

    private fun checkItemIndex(index: Int) {
        require(index in 0 until capacity) {
            "Index $index out of bounds [0..${capacity - 1}]"
        }
    }
}
