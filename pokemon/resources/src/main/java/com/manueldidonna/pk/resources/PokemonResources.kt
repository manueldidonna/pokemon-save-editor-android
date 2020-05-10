package com.manueldidonna.pk.resources

import com.manueldidonna.pk.resources.english.EnglishPokemonResources

interface PokemonResources {
    val natures: Natures

    interface Natures {
        fun getNatureById(id: Int): String
    }

    val moves: Moves

    interface Moves {
        fun getMoveById(id: Int): String
        fun getAllMoves(): List<String>
    }

    companion object {
        val English: PokemonResources = EnglishPokemonResources()
    }
}