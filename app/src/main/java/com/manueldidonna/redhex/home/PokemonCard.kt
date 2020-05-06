package com.manueldidonna.redhex.home

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.ColorFilter
import androidx.ui.layout.*
import androidx.ui.material.Emphasis
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ripple.ripple
import androidx.ui.res.imageResource
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import com.manueldidonna.redhex.R
import com.manueldidonna.redhex.common.PreviewScreen

data class PokemonPreview(
    val slot: Int,
    val nickname: String,
    val level: Int
)

@Composable
fun PokemonCard(name: String, level: Int) {
    val emphasis = EmphasisAmbient.current.run { if (level > 0) medium else disabled }
    Row(
        modifier = Modifier.fillMaxWidth().preferredHeight(56.dp),
        verticalGravity = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.preferredWidth(24.dp))
        Image(
            modifier = Modifier.preferredSize(32.dp),
            colorFilter = ColorFilter.tint(emphasis.emphasize(MaterialTheme.colors.onSurface)),
            asset = imageResource(R.drawable.pokeball_s) // TODO: use pokemon sprites
        )
        Text(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp),
            text = name,
            color = emphasis.emphasize(MaterialTheme.colors.onSurface),
            style = MaterialTheme.typography.body1
        )
        if (level > 0)
            CardLabel(
                modifier = Modifier.padding(end = 8.dp),
                text = "Lv. $level",
                emphasis = emphasis
            )
    }
}

@Composable
private fun CardLabel(modifier: Modifier = Modifier, text: String, emphasis: Emphasis) {
    Box(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2f),
        paddingBottom = 4.dp,
        paddingTop = 4.dp,
        paddingEnd = 8.dp,
        paddingStart = 8.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.subtitle2.copy(fontSize = 12.sp),
            color = emphasis.emphasize(MaterialTheme.colors.onSurface)
        )
    }
}


@Preview
@Composable
private fun PreviewPokemonCard() {
    PreviewScreen(isLightTheme = true) {
        PokemonCard(name = "PIKACHU", level = 10)
    }
}