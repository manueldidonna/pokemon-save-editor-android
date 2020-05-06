package com.manueldidonna.pk.core

/**
 * Write pokemon data values. If you need an immutable read-only entity use [Pokemon]
 */
interface PokemonWriter {
    fun speciesId(value: Int): PokemonWriter
    fun nickname(value: String): PokemonWriter
    fun trainerId(value: UInt): PokemonWriter
    fun trainerName(value: String): PokemonWriter
}