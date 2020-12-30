package com.manueldidonna.redhex.common

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Item

val ItemSpriteModifier: Modifier = Modifier
    .padding(start = 16.dp, end = 20.dp)
    .preferredSize(40.dp, 32.dp)

val PokemonSpriteModifier: Modifier =  Modifier
    .padding(start = 16.dp, end = 20.dp)
    .preferredSize(46.dp, 34.dp)

interface SpritesRetriever {
    fun getPokemonSprite(speciesId: Int, shiny: Boolean): Any
    fun getItemSprite(itemId: Int): Any
}

object AssetsSpritesRetriever : SpritesRetriever {
    private const val assetPath = "file:///android_asset/"

    override fun getItemSprite(itemId: Int): Any {
        val asset = when {
            itemId == Item.BikeVoucherId -> "item_bike_voucher.png"
            itemId in Item.FlowerMailId..Item.MirageMailId -> "item_143.png"
            Item.isHiddenMachine(itemId) -> "item_332.png"
            Item.isTechnicalMachine(itemId) -> "item_328.png"
            else -> "item_${itemId}.png"
        }
        return Uri.parse(assetPath + asset)
    }

    override fun getPokemonSprite(speciesId: Int, shiny: Boolean): Any {
        val base = if (shiny) "pk_shiny" else "pk"
        return Uri.parse("$assetPath${base}_$speciesId.png")
    }
}
