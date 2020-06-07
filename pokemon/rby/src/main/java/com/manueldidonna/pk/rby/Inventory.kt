package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.Inventory as CoreInventory

internal class Inventory(
    override val type: CoreInventory.Type,
    private val data: UByteArray
) : CoreInventory {

    override val maxAllowedQuantity: Int = 99

    override val supportedItemIds = SupportedItemIds

    override val capacity: Int

    private val startOffset: Int

    init {
        when (type) {
            CoreInventory.Type.General -> {
                capacity = 20
                startOffset = BagOffset
            }
            CoreInventory.Type.Computer -> {
                capacity = 50
                startOffset = ComputerOffset
            }
            else -> throw IllegalStateException("Inventory Type not supported: $type")
        }
    }

    override var size: Int
        get() {
            val size = data[startOffset].toInt()
            // uninitialized yellow (0xFF), sanity check for out-of-bounds values
            return if (size > capacity) 0 else size
        }
        private set(value) {
            data[startOffset] = value.coerceIn(0, capacity).toUByte()
            // end byte
            data[startOffset + (value * 2 + 1)] = 0xFFu
        }

    override fun getItem(index: Int): CoreInventory.Item {
        require(index in 0 until capacity) {
            "Index ($index) out of Inventory bounds [0-$capacity]"
        }
        if (index >= size) size++
        @Suppress("NAME_SHADOWING")
        val index = index.coerceAtMost(size)
        return CoreInventory.Item(
            index = index,
            id = data[startOffset + (index * 2) + 1].toInt(),
            quantity = data[startOffset + (index * 2) + 2].toInt()
        )
    }

    override fun setItem(item: CoreInventory.Item, index: Int) {
        require(index in 0 until capacity) {
            "Index ($index) out of Inventory bounds [0-$capacity]"
        }
        // delete item at specified index
        if (item.id == 0 || item.quantity == 0) {
            val lastIndexOffset = startOffset + 1 + (capacity - 1) * 2
            //shift items left of 1 position
            if (index < capacity - 1) {
                val destinationOffset = startOffset + 1 + (index) * 2
                val startShiftOffset = startOffset + 1 + (index + 1) * 2
                data.copyInto(data, destinationOffset, startShiftOffset, lastIndexOffset + 2)
            }
            data.fill(0u, lastIndexOffset, lastIndexOffset + 2)
            size--
        } else {
            require(item.id in supportedItemIds) {
                "Item id (${item.id}) is not supported"
            }
            if (index >= size) size++
            val offset = startOffset + (index.coerceAtMost(size) * 2) + 1
            data[offset] = item.id.toUByte()
            data[offset + 1] = item.quantity.coerceAtMost(maxAllowedQuantity).toUByte()
        }
    }

    companion object {
        private const val BagOffset = 0x25C9
        private const val ComputerOffset = 0x27E6
        private val SupportedItemIds: List<Int> by lazy {
            listOf(
                1, 2, 3, 4, 5, 6, 10, 11, 12, 13, 14, 15,
                16, 17, 18, 19, 20, 29, 30, 31,
                32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 45, 46, 47,
                48, 49, 51, 52, 53, 54, 55, 56, 57, 58, 60, 61, 62, 63,
                64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
                80, 81, 82, 83,
                // ...
                196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207,
                208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220,
                221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233,
                234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246,
                247, 248, 249, 250
            )
        }
    }
}
