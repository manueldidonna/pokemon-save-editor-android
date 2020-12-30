package com.manueldidonna.redhex.ui.pokemon

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.AmbientContentColor
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.manueldidonna.redhex.R
import com.manueldidonna.redhex.common.PokemonSpriteModifier
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun PokemonList(
    modifier: Modifier = Modifier,
    pokemonEntities: List<PokemonEntity?>,
    onEntityClick: (index: Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
        modifier = modifier,
    ) {
        itemsIndexed(pokemonEntities) { index, entity ->
            PokemonEntity(entity) { onEntityClick(index) }
        }
    }
}

@Composable
private fun PokemonEntity(entity: PokemonEntity?, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(56.dp)
            .clickable(
                onClick = onClick,
                indication = rememberRipple(color = MaterialTheme.colors.primary)
            )
    ) {
        val disabledColor = AmbientContentColor.current.copy(alpha = ContentAlpha.disabled)
        CoilImage(
            data = entity?.spriteSource ?: R.drawable.pokeball_s,
            modifier = PokemonSpriteModifier,
            colorFilter = if (entity == null) ColorFilter.tint(disabledColor) else null
        )
        Text(
            text = entity?.nickname ?: "Empty slot",
            color = if (entity == null) disabledColor else AmbientContentColor.current,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(1f)
        )
    }
}
