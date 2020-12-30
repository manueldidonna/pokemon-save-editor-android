package com.manueldidonna.pk.core

/**
 * Version should be used primarily to retrieve the correct game resources.
 * The logic to manipulate the data shouldn't depends on this class
 */
enum class Version(val value: Int) {
    Red(35),
    BlueGreen(36),
    Yellow(38),
    Gold(39),
    Silver(40),
    Crystal(41)
}

val Version.generation: Int
    get() = when (value) {
        in 35..38 -> 1
        in 39..41 -> 2
        else -> throw IllegalStateException("Unsupported Version value: $value")
    }
