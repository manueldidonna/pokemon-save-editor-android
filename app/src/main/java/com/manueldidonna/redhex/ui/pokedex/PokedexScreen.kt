package com.manueldidonna.redhex.ui.pokedex

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.manueldidonna.pk.core.Pokedex

@Composable
fun PokedexScreen(pokedex: Pokedex, goBack: () -> Unit) {
    val statefulPokedex = rememberStatefulPokedex(pokedex)
    Column {
        PokedexAppBar(
            goBack = goBack,
            filterPokemonNames = { /*TODO*/ }
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
