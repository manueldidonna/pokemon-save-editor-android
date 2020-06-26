package com.manueldidonna.redhex.common

import android.net.Uri
import androidx.ui.core.Modifier
import androidx.ui.layout.preferredSize
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.Inventory

val PokemonSpriteSize: Modifier = Modifier.preferredSize(40.dp, 32.dp)

val ItemSpriteSize: Modifier = Modifier.preferredSize(32.dp)

inline class SpriteSource(val value: Any) {

    companion object {
        val Invalid = SpriteSource("")
    }
}

interface SpritesRetriever {
    fun getPokemonSprite(speciesId: Int): SpriteSource
    fun getItemSprite(itemId: Int): SpriteSource
}

object AssetsSpritesRetriever : SpritesRetriever {
    private const val assetPath = "file:///android_asset/"

    override fun getItemSprite(itemId: Int): SpriteSource {
        val asset = when {
            itemId == Inventory.Item.BikeVoucherId -> "item_bike_voucher.png"
            Inventory.Item.isHiddenMachine(itemId) -> "item_332.png"
            Inventory.Item.isTechnicalMachine(itemId) -> "item_328.png"
            else -> "item_${itemId}.png"
        }
        return SpriteSource(Uri.parse(assetPath + asset))
    }

    override fun getPokemonSprite(speciesId: Int): SpriteSource {
        return SpriteSource(Uri.parse("${assetPath}pk_$speciesId.png"))
    }
}
