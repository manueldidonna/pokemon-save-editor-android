package com.manueldidonna.redhex.common

import android.net.Uri
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.Items
import com.manueldidonna.redhex.R
import dev.chrisbanes.accompanist.coil.CoilImage

val PokemonSpriteSize: Modifier = Modifier.preferredSize(40.dp, 32.dp)

val ItemSpriteSize: Modifier = Modifier.preferredSize(32.dp)

@Immutable
inline class SpriteSource(val value: Any) {
    companion object {
        val PokeBall = SpriteSource(R.drawable.pokeball_s)
    }
}

@Composable
fun PokemonSprite(source: SpriteSource) {
    var colorFilter: ColorFilter? = null
    if (source.value == SpriteSource.PokeBall.value) {
        val emphasis = EmphasisAmbient.current.disabled
        colorFilter = ColorFilter.tint(emphasis.applyEmphasis(MaterialTheme.colors.onSurface))
    }
    CoilImage(
        modifier = PokemonSpriteSize,
        colorFilter = colorFilter,
        data = source.value
    )
}

interface SpritesRetriever {
    fun getPokemonSprite(speciesId: Int, shiny: Boolean): SpriteSource
    fun getItemSprite(itemId: Int): SpriteSource
}

object AssetsSpritesRetriever : SpritesRetriever {
    private const val assetPath = "file:///android_asset/"

    override fun getItemSprite(itemId: Int): SpriteSource {
        val asset = when {
            itemId == Items.BikeVoucherId -> "item_bike_voucher.png"
            itemId in Items.FlowerMailId..Items.MirageMailId -> "item_143.png"
            Inventory.Item.isHiddenMachine(itemId) -> "item_332.png"
            Inventory.Item.isTechnicalMachine(itemId) -> "item_328.png"
            else -> "item_${itemId}.png"
        }
        return SpriteSource(Uri.parse(assetPath + asset))
    }

    override fun getPokemonSprite(speciesId: Int, shiny: Boolean): SpriteSource {
        val base = if (shiny) "pk_shiny" else "pk"
        return SpriteSource(Uri.parse("${assetPath}${base}_$speciesId.png"))
    }
}
