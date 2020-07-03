@file:Suppress("NOTHING_TO_INLINE")

package com.manueldidonna.pk.core

/**
 * Property is a wrapper around pokemon properties that may not exist in some games
 */
sealed class Property<out T> {
    /**
     * The property doesn't exist
     */
    object Nothing : Property<kotlin.Nothing>()

    /**
     * The property exists
     */
    data class Value<T : Any>(val value: T) : Property<T>()
}

inline fun <T : Any> T.asProperty(): Property<T> = Property.Value(this)

inline fun <T : Any> Property<T>.valueOrNull(): Property.Value<T>? = this as? Property.Value<T>
