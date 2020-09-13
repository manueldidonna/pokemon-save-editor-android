package com.manueldidonna.pk.rby

import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.core.isEmpty
import com.manueldidonna.pk.core.Pokemon as CorePokemon

internal class PokemonFactory(private val version: Version) : CorePokemon.Factory {
    override fun create(
        template: CorePokemon.Template,
        position: CorePokemon.Position,
    ): CorePokemon? {
        val emptyPokemon = Pokemon(
            data = UByteArray(Storage.PokemonSizeInBoxWithNames),
            version = version,
            storageIndex = position.storageIndex,
            pokemonIndex = position.pokemonIndex
        )
        val pokemon = template.applyTo(emptyPokemon)
        return if (pokemon.isEmpty()) null else pokemon
    }
}
