package com.manueldidonna.pk.rby.converter

import com.manueldidonna.pk.core.Items

/**
 * Unused id for items
 */
private const val NaN = 128

internal fun getGameBoyItemId(valueToConvert: Int): Int {
    val index = GameBoyToUniversalItemIds.indexOf(valueToConvert)
    if (index == -1) {
        throw IllegalStateException("GameBoy Item ID not found for $valueToConvert")
    }
    return index
}

internal fun getUniversalItemId(valueToConvert: Int): Int {
    val value = GameBoyToUniversalItemIds[valueToConvert]
    if (value == NaN) {
        throw IllegalStateException("Passed an invalid GameBoy Item ID: $valueToConvert")
    }
    return value
}

/**
 * Items supported by gen 1 games (bag and pc)
 */
internal val SupportedItemIds: List<Int> by lazy {
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
    ).map { getUniversalItemId(it) }
}

/**
 * key: GameBoy ID
 * value: Universal ID
 */
private val GameBoyToUniversalItemIds = intArrayOf(
    0, 1, 2, 3, 4, 442, 450, NaN, NaN, NaN, 81, 18, 19, 20, 21, 22, 23, 24, 25,
    26, 17, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, 78, 79, 103, 82, 83, 84, 45,
    46, 47, 48, 49, 50, 102, 101, 467, NaN, Items.BikeVoucherId, 60, 85, 475, 92, NaN,
    63, 27, 28, 29, 55, 76, 77, 56, NaN, 30, 31, 32, 456, 877, 57, 58, 59, 61,
    444, 459, 471, 874, 651, 878, 216, 445, 446, 447, 51, 38, 39, 40, 41, NaN, NaN,
    NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN,
    NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN,
    NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN,
    NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN,
    NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN,
    NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN,
    NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN, NaN,
    // HMs
    420, 421, 422, 423, 424,
    // TMs
    328, 329, 330, 331, 332, 333, 334, 335, 336, 337, 338, 339, 340, 341, 342, 343, 344, 345, 346,
    347, 348, 349, 350, 351, 352, 353, 354, 355, 356, 357, 358, 359, 360, 361, 362, 363, 364, 365,
    366, 367, 368, 369, 370, 371, 372, 373, 374, 375, 376, 377
)
