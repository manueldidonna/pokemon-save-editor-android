package com.manueldidonna.pk.core

/**
 * Version should be used primarily to retrieve the correct game resources.
 * The logic to manipulate the data shouldn't depends on this class
 */
enum class Version(val value: Int) {
    RedBlue(100), // todo: use VC value instead of arbitrary numbers
    Yellow(101)
}

inline val Version.isFirstGeneration: Boolean
    get() = this == Version.RedBlue || this == Version.Yellow
