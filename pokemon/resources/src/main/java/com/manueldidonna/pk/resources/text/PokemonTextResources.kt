package com.manueldidonna.pk.resources.text

import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.resources.text.english.EnglishPokemonTextResources

interface PokemonTextResources {
    val natures: Natures

    interface Natures {
        fun getNatureById(id: Int): String
    }

    val moves: Moves

    interface Moves {
        fun getMoveById(id: Int): String
        fun getAllMoves(version: Version): List<String>
    }

    val species: Species

    interface Species {
        fun getSpeciesById(id: Int): String
        fun getAllSpecies(version: Version): List<String>
    }

    val items: Items

    interface Items {
        fun getAllItems(): List<String>
        fun getTypeName(type: Inventory.Type): String
    }

    companion object {
        val English: PokemonTextResources = EnglishPokemonTextResources()
    }
}