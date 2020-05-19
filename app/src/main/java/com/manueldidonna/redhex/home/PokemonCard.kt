package com.manueldidonna.redhex.home

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.graphics.ColorFilter
import androidx.ui.layout.*
import androidx.ui.material.Emphasis
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.MaterialTheme
import androidx.ui.res.imageResource
import androidx.ui.unit.dp
import com.manueldidonna.redhex.R
import com.manueldidonna.redhex.common.pokemon.PokemonSpriteSize
import com.manueldidonna.redhex.common.pokemon.pokemonSpriteSize
import com.manueldidonna.redhex.common.ui.Label
import dev.chrisbanes.accompanist.coil.CoilImage
import java.io.File

data class PokemonPreview(
    val nickname: String,
    val labels: List<String>,
    val sprite: File
)

@Composable
fun PokemonCard(preview: PokemonPreview?) {
    val emphasis = EmphasisAmbient.current.run { if (preview != null) medium else disabled }
    Row(
        modifier = Modifier.fillMaxWidth().preferredHeight(56.dp),
        verticalGravity = Alignment.CenterVertically
    ) {
        if (preview == null) {
            PokeballPlaceholder(emphasis = emphasis)
        } else {
            LoadPokemonSprite(data = preview.sprite)
        }
        Text(
            modifier = Modifier.padding(end = 24.dp),
            text = preview?.nickname ?: "Empty Slot",
            color = emphasis.emphasize(MaterialTheme.colors.onSurface),
            style = MaterialTheme.typography.body1
        )
        if (preview != null && preview.labels.isNotEmpty())
            preview.labels.forEach { label ->
                Label(
                    text = label,
                    emphasis = emphasis,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
    }
}

@Composable
private fun PokeballPlaceholder(emphasis: Emphasis) {
    Spacer(modifier = Modifier.preferredWidth(16.dp))
    Image(
        modifier = Modifier.pokemonSpriteSize(),
        colorFilter = ColorFilter.tint(emphasis.emphasize(MaterialTheme.colors.onSurface)),
        asset = imageResource(R.drawable.pokeball_s)
    )
    Spacer(modifier = Modifier.preferredWidth(16.dp))
}

@Composable
private fun LoadPokemonSprite(data: Any) {
    Spacer(modifier = Modifier.preferredWidth(16.dp))
    CoilImage(
        data = data,
        modifier = Modifier.pokemonSpriteSize()
    )
    Spacer(modifier = Modifier.preferredWidth(16.dp))
}
