package com.manueldidonna.redhex.pokemonlist

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.manueldidonna.pk.core.MutableStorage
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.SpritesRetriever

@Stable
class ObservableStorage(
    private val storage: MutableStorage,
    private val resources: PokemonTextResources.Natures,
    private val spritesRetriever: SpritesRetriever,
) : MutableStorage by storage {

    val entries: SnapshotStateList<PokemonEntry?> = mutableStateListOf()

    override fun removeAt(index: Int) {
        storage.removeAt(index)
        fetchAll()
    }

    override fun set(index: Int, pokemon: Pokemon) {
        storage[index] = pokemon
        updateAt(index)
    }

    private fun updateAt(index: Int) {
        entries[index] = PokemonEntry.fromPokemon(storage[index], resources, spritesRetriever)
    }

    fun fetchAll() {
        entries.clear()
        entries.addAll(PokemonEntry.fromStorage(storage, resources, spritesRetriever))
    }
}
