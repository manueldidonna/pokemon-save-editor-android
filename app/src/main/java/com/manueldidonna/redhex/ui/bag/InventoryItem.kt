package com.manueldidonna.redhex.ui.bag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.pk.core.Item
import com.manueldidonna.redhex.AmbientPokemonTextResources
import com.manueldidonna.redhex.AmbientSpritesRetriever

@Immutable
data class InventoryItem(
    override val id: Int,
    override val quantity: Int,
    val name: String,
    val spriteSource: Any,
) : Item

@Composable
fun inventoryItemMapper(): Inventory.ItemMapper<InventoryItem> {
    val pokemonTextResources = AmbientPokemonTextResources.current.items
    val spritesRetriever = AmbientSpritesRetriever.current
    return remember(pokemonTextResources, spritesRetriever) {
        Inventory.ItemMapper { id, quantity ->
            InventoryItem(
                id = id,
                quantity = quantity,
                name = pokemonTextResources.getItemById(id),
                spriteSource = spritesRetriever.getItemSprite(id)
            )
        }
    }
}
