package com.manueldidonna.pk.core

/**
 * Version should be used primarily to retrieve the correct game resources.
 * The logic to manipulate the data shouldn't depends on this class
 */
enum class Version(val value: Int) {
    Red(35),
    Blue(36), // The same id is used for Green[only JP]
    Yellow(38),
    Gold(39),
    Silver(40),
    Crystal(41)
}

inline val Version.isFirstGeneration: Boolean
    get() = this.value in 35..38

inline val Version.isSecondGeneration: Boolean
    get() = this.value in 39..41

inline val Version.generation: Int
    get() = when (this.value) {
        in 35..38 -> 1
        in 39..41 -> 2
        else -> throw IllegalStateException()
    }
