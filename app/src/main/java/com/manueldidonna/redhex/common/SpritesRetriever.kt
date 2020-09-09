package com.manueldidonna.redhex.common

import android.net.Uri
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.Items
import com.manueldidonna.redhex.R

@Immutable
inline class SpriteSource(val value: Any) {
    companion object {
        val Size: Modifier = Modifier.preferredSize(40.dp, 32.dp)
        val PokeBall = SpriteSource(R.drawable.pokeball_s)
    }
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
