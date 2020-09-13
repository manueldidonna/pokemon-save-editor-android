package com.manueldidonna.pk.core

/**
 * A collection of [Storage] with a fixed capacity given by [storageIndices] size.
 */
interface StorageSystem {
    /**
     * Indices of the [Storage] contained in the system.
     */
    val storageIndices: IntRange

    /**
     * Return a [Storage] instance.
     *
     * Should throw an [IllegalStateException] if [index] isn't contained in [storageIndices]
     */
    operator fun get(index: Int): Storage

    /**
     * Replaces the storage at the specified [index] with the specified [storage].
     *
     * Should throw an [IllegalStateException] if [index] isn't contained in [storageIndices]
     */
    operator fun set(index: Int, storage: Storage)

    companion object {
        /**
         * The index of a Party, a particular kind of [Storage]
         */
        const val PartyIndex: Int = -1
    }
}

inline val Int.isPartyIndex: Boolean get() = this == StorageSystem.PartyIndex

/**
 * Get a [MutableStorage] through [action] that will automatically set in the system
 * after the lambda block execution.
 */
inline fun StorageSystem.mutate(index: Int, action: (storage: MutableStorage) -> Unit) {
    val storage = get(index).toMutableStorage()
    action(storage)
    set(index, storage)
}

operator fun StorageSystem.get(position: Pokemon.Position): Pokemon? {
    return get(position.storageIndex)[position.pokemonIndex]
}

operator fun StorageSystem.set(position: Pokemon.Position, pokemon: Pokemon) {
    mutate(position.storageIndex) {
        it[position.pokemonIndex] = pokemon
    }
}

/**
 * TODO: this function is never used
 */
private fun StorageSystem.swapPokemon(first: Pokemon.Position, second: Pokemon.Position) {
    if (first == second) return // do nothing
    val firstPokemon = get(first)
    val secondPokemon = get(second)
    mutate(first.storageIndex) {
        it.setOrRemove(first.pokemonIndex, secondPokemon)
    }
    mutate(second.storageIndex) {
        it.setOrRemove(second.pokemonIndex, firstPokemon)
    }
}
