package com.manueldidonna.redhex.ui.pokedex

import androidx.compose.runtime.*
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.pk.core.pokemonCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun rememberStatefulPokedex(pokedex: Pokedex): StatefulPokedex {
    val entryMapper = pokedexEntryMapper()
    val statefulPokedex = remember(entryMapper, pokedex) {
        StatefulPokedex(pokedex, entryMapper)
    }
    LaunchedEffect(subject = statefulPokedex) {
        withContext(Dispatchers.Default) {
            statefulPokedex.fetchEntries()
        }
    }
    return statefulPokedex
}

@Stable
class StatefulPokedex(
    private val pokedex: Pokedex,
    private val entryMapper: Pokedex.EntryMapper<PokedexEntry>
) : Pokedex by pokedex {

    private val _entries = mutableStateListOf<PokedexEntry>()
    val entries: List<PokedexEntry> get() = _entries

    private val _pokemonSeenCount = mutableStateOf(0)
    val pokemonSeenCount get() = _pokemonSeenCount.value

    override fun setEntry(entry: Pokedex.Entry) {
        val oldEntry = _entries[entry.speciesId - 1]
        pokedex.setEntry(entry)
        val newEntry = pokedex.selectEntry(entry.speciesId, entryMapper)
        updateSeenCount(oldEntry = oldEntry, newEntry = newEntry)
        _entries[entry.speciesId - 1] = newEntry
    }

    private fun updateSeenCount(oldEntry: Pokedex.Entry, newEntry: Pokedex.Entry) {
        if (oldEntry.isSeen == newEntry.isSeen) return
        _pokemonSeenCount.value += if (newEntry.isSeen) 1 else -1
    }

    fun fetchEntries() {
        if (_entries.isEmpty()) _entries.clear()
        val firstSpeciesId = pokedex.pokemonSpeciesIds.first
        _entries.addAll(List(size = pokemonCount) {
            pokedex.selectEntry(speciesId = firstSpeciesId + it, entryMapper)
        })
        _pokemonSeenCount.value = _entries.count { it.isSeen }
    }
}