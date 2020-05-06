package com.manueldidonna.pk.resources.english

import com.manueldidonna.pk.resources.PokemonResources

internal class EnglishNatures : PokemonResources.Natures {
    private val values = arrayOf(
        "Hardy",
        "Lonely",
        "Brave",
        "Adamant",
        "Naughty",
        "Bold",
        "Docile",
        "Relaxed",
        "Impish",
        "Lax",
        "Timid",
        "Hasty",
        "Serious",
        "Jolly",
        "Naive",
        "Modest",
        "Mild",
        "Quiet",
        "Bashful",
        "Rash",
        "Calm",
        "Gentle",
        "Sassy",
        "Careful",
        "Quirky"
    )

    override fun getNatureById(id: Int): String {
        require(id in 0..24) { "Nature id must be 0..24" }
        return values[id]
    }
}
