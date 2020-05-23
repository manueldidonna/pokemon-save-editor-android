package com.manueldidonna.pk.core

/**
 * Version should be used primarily to retrieve different game resources.
 * The logic to manipulate the data shouldn't depends on this interface
 */
interface Version {
    val index: Int

    data class FirstGeneration(val isYellow: Boolean) : Version {
        override val index: Int = 0
    }
}
