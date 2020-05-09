package com.manueldidonna.redhex.home

import androidx.compose.Composable
import androidx.ui.core.Alignment
import androidx.ui.core.ContentScale
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
import androidx.ui.res.imageResource
import androidx.ui.unit.dp
import androidx.ui.unit.sp
import com.manueldidonna.redhex.R
import com.manueldidonna.redhex.common.ui.LoadImage
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
        LoadPokemonSprite(emphasis = emphasis, data = preview?.sprite)
        Text(
            modifier = Modifier.padding(end = 24.dp),
            text = preview?.nickname ?: "Empty Slot",
            color = emphasis.emphasize(MaterialTheme.colors.onSurface),
            style = MaterialTheme.typography.body1
        )
        if (preview != null && preview.labels.isNotEmpty())
            preview.labels.forEach { label ->
                CardLabel(
                    text = label,
                    emphasis = emphasis,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
    }
}

@Composable
private fun LoadPokemonSprite(emphasis: Emphasis, data: Any?) {
    val image = if (data == null) null else LoadImage(data = data)
    val spacerWidth = Modifier.preferredWidth(if (image == null) 24.dp else 16.dp)
    Spacer(modifier = spacerWidth)
    if (image != null) {
        Image(
            modifier = Modifier.preferredWidth(48.dp).aspectRatio(4f / 3f),
            asset = image,
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            modifier = Modifier.preferredSize(32.dp),
            colorFilter = ColorFilter.tint(emphasis.emphasize(MaterialTheme.colors.onSurface)),
            asset = imageResource(R.drawable.pokeball_s)
        )
    }
    Spacer(modifier = spacerWidth)
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
