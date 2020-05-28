package com.manueldidonna.pk.core

/**
 * Version should be used primarily to retrieve the correct game resources.
 * The logic to manipulate the data shouldn't depends on this interface
 */
sealed class Version {
    abstract val index: Int

    data class FirstGeneration(val isYellow: Boolean) : Version() {
        override val index: Int = 0
    }
}
