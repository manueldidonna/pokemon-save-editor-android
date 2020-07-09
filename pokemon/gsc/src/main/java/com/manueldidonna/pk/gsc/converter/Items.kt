package com.manueldidonna.pk.gsc.converter

import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.Items.BerserkGeneId
import com.manueldidonna.pk.core.Items.BlueSkyMailId
import com.manueldidonna.pk.core.Items.BrickPieceId
import com.manueldidonna.pk.core.Items.EggTicketId
import com.manueldidonna.pk.core.Items.EonMailId
import com.manueldidonna.pk.core.Items.FlowerMailId
import com.manueldidonna.pk.core.Items.GSBallId
import com.manueldidonna.pk.core.Items.GorgeousBoxId
import com.manueldidonna.pk.core.Items.LiteBlueMailId
import com.manueldidonna.pk.core.Items.LovelyMailId
import com.manueldidonna.pk.core.Items.MirageMailId
import com.manueldidonna.pk.core.Items.MorphMailId
import com.manueldidonna.pk.core.Items.MusicMailId
import com.manueldidonna.pk.core.Items.NormalBoxId
import com.manueldidonna.pk.core.Items.PinkBowId
import com.manueldidonna.pk.core.Items.PolkadotBowId
import com.manueldidonna.pk.core.Items.PortraitMailId
import com.manueldidonna.pk.core.Items.SurfMailId

private const val NaN = -1

internal fun getLocalItemId(valueToConvert: Int): Int {
    val index = LocalToUniversalItemIds.indexOf(valueToConvert)
    if (index == -1) {
        throw IllegalStateException("Local Item ID not found for $valueToConvert")
    }
    return index
}

internal fun getUniversalItemId(valueToConvert: Int): Int {
    val value = LocalToUniversalItemIds[valueToConvert]
    if (value == NaN) {
        println("Passed an invalid Local Item ID: $valueToConvert")
        return value
    }
    return value
}

internal fun getIdsByType(type: Inventory.Type, isCrystal: Boolean): List<Int> {
    return when (type) {
        Inventory.Type.General -> if (isCrystal) Items + CrystalExclusiveKeys else Items
        Inventory.Type.Computer -> AllItems
        Inventory.Type.Balls -> Balls
        Inventory.Type.Keys -> if (isCrystal) Keys + CrystalExclusiveKeys else Keys
        Inventory.Type.HiddenMachines -> HiddenMachines
        Inventory.Type.TechnicalMachines -> TechnicalMachines
        else -> throw IllegalArgumentException("Unsupported type $type")
    }
}

private val CrystalExclusiveKeys = listOf(70, 115, 116, 129)

private val AllItems: List<Int> by lazy { LocalToUniversalItemIds.filter { it != NaN } }

private val Balls: List<Int> by lazy {
    listOf(1, 2, 4, 5, 157, 159, 160, 161, 164, 165, 166).map { getUniversalItemId(it) }
}

private val Items: List<Int> by lazy {
    listOf(
        3, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 26, 27, 28,
        29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 46, 47, 48, 49, 51,
        52, 53, 57, 60, 62, 63, 64, 65, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84,
        85, 86, 87, 88, 89, 91, 92, 93, 94, 95, 96, 97, 98, 99, 101, 102, 103, 104, 105,
        106, 107, 108, 109, 110, 111, 112, 113, 114, 117, 118, 119, 121, 122, 123, 124,
        125, 126, 131, 132, 138, 139, 140, 143, 144, 146, 150, 151, 152, 156, 158, 163,
        167, 168, 169, 170, 172, 173, 174, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189
    ).map { getUniversalItemId(it) }
}

private val Keys: List<Int> by lazy {
    listOf(
        7, 54, 55, 58, 59, 61, 66, 67, 68, 69, 71, 127, 128, 130, 133, 134, 175, 178
    ).map { getUniversalItemId(it) }
}

private val TechnicalMachines: List<Int> by lazy {
    listOf(
        191, 192, 193, 194, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207,
        208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 221, 222, 223, 224,
        225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240,
        241, 242
    ).map { getUniversalItemId(it) }
}

private val HiddenMachines: List<Int> by lazy {
    listOf(243, 244, 245, 246, 247, 248, 249).map { getUniversalItemId(it) }
}

/**
 * index: Local ID
 * value: Universal ID
 */
private val LocalToUniversalItemIds = intArrayOf(
    0, 1, 2, 213, 3, 4, NaN, 450, 81, 18, // 0
    19, 20, 21, 22, 23, 24, 25, 26, 17, 78, // 1
    79, 41, 82, 83, 84, NaN, 45, 46, 47, 48, // 2
    256, 49, 50, 60, 85, 257, 92, 63, 27, 28, // 3
    29, 55, 76, 77, 56, NaN, 30, 31, 32, 57, // 4
    NaN, 58, 59, 61, 444, 471, NaN, 216, 445, 446, // 5
    891, 447, 51, 38, 39, 40, 478, 464, 456, 484, // 6
    474, 482, 33, 217, 151, 890, 237, 244, 149, 153, // 7
    152, 245, 221, 156, 150, 485, 86, 87, 222, 487, // 8
    NaN, 223, 486, 488, 224, 243, 248, 490, 241, 491, // 9
    NaN, 489, 240, 473, PinkBowId, 259, 228, 246, 242, 157, // 10
    88, 89, 229, 247, 504, GSBallId, 472, 239, 258, 230, // 11
    NaN, 34, 35, 36, 37, 238, 231, 475, 481, EggTicketId, // 12
    479, 90, 91, 476, 480, NaN, NaN, NaN, 249, 43, // 13
    232, NaN, NaN, 233, 250, NaN, 234, NaN, NaN, NaN, // 14
    154, 235, BerserkGeneId, NaN, NaN, NaN, 44, 495, FlowerMailId, 493, // 15
    494, 492, NaN, 236, 497, 498, 496, NormalBoxId, GorgeousBoxId, 80, // 16
    PolkadotBowId, NaN, 252, 155, 158, 477, NaN, 500, 483, NaN, // 17
    BrickPieceId, SurfMailId, LiteBlueMailId, PortraitMailId, LovelyMailId, EonMailId,
    MorphMailId, BlueSkyMailId, MusicMailId, MirageMailId, // 18
    NaN, 328, 329, 330, 331, 331, 332, 333, 334, 335, // 19
    336, 337, 338, 339, 340, 341, 342, 343, 344, 345, // 20
    346, 347, 348, 349, 350, 351, 352, 353, 354, 355, // 21
    355, 356, 357, 358, 359, 360, 361, 362, 363, 364, // 22
    365, 366, 367, 368, 369, 370, 371, 372, 373, 374, // 23
    375, 376, 377, 420, 421, 422, 423, 424, 425, 426, // 24
    427, NaN, NaN, NaN, NaN, NaN
)
