package com.manueldidonna.redhex.inventory

import androidx.compose.runtime.Immutable
import com.manueldidonna.pk.core.Inventory
import com.manueldidonna.redhex.common.SpriteSource

@Immutable
data class InventoryItem(
    override val index: Int,
    override val id: Int,
    override val quantity: Int,
    val name: String,
    val spriteSource: SpriteSource,
) : Inventory.Item {
    companion object {
        val Invalid = InventoryItem(-1, 0, 0, "", SpriteSource.PokeBall)
        fun empty(index: Int) = InventoryItem(index, 0, 0, "", SpriteSource.PokeBall)
    }
}
