package com.manueldidonna.redhex.ui.pokedex

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.manueldidonna.pk.core.Pokedex
import com.manueldidonna.pk.core.setAllCaught
import com.manueldidonna.pk.core.setAllSeen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun PokedexScreen(pokedex: Pokedex, goBack: () -> Unit) {
    val statefulPokedex = rememberStatefulPokedex(pokedex)
    val scope = rememberCoroutineScope()
    Column {
        PokedexAppBar(
            goBack = goBack,
            filterPokemonNames = { /*TODO*/ },
            setAllPokemonCaught = {
                scope.launch(Dispatchers.Default) { statefulPokedex.setAllCaught() }
            },
            setAllPokemonSeen = {
                scope.launch(Dispatchers.Default) { statefulPokedex.setAllSeen() }
            }
        )
        PokedexEntriesList(
            pokedexEntries = statefulPokedex.entries,
            pokemonSeenCount = statefulPokedex.pokemonSeenCount,
            onEntryChange = { entry ->
                statefulPokedex.setEntry(entry)
            }
        )
    }
}
