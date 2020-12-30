package com.manueldidonna.redhex.ui.bag

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.AmbientContentColor
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manueldidonna.redhex.common.ItemSpriteModifier
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun BagItemsList(
    listState: LazyListState = rememberLazyListState(),
    inventoryItems: List<InventoryItem>,
    inventoryCapacity: Int,
    contentPadding: PaddingValues,
    onItemClick: (index: Int) -> Unit
) {
    if (inventoryItems.isEmpty()) return
    LazyColumn(contentPadding = contentPadding, state = listState) {
        item {
            Text(
                text = "${inventoryItems.size} out of $inventoryCapacity slots occupied",
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.body2,
                color = AmbientContentColor.current.copy(alpha = ContentAlpha.medium),
                modifier = Modifier.padding(bottom = 16.dp, start = 24.dp, top = 8.dp),
            )
        }
        itemsIndexed(inventoryItems) { index, item ->
            InventoryItem(
                spriteSource = item.spriteSource,
                name = item.name,
                quantity = item.quantity,
                onClick = { onItemClick(index) }
            )
        }
    }
}

@Composable
private fun InventoryItem(
    spriteSource: Any,
    name: String,
    quantity: Int,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(56.dp)
            .clickable(
                onClick = onClick,
                indication = rememberRipple(color = MaterialTheme.colors.primary)
            )
    ) {
        CoilImage(data = spriteSource, modifier = ItemSpriteModifier)
        Text(
            text = name,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Qt. $quantity",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary,
        )
        Spacer(Modifier.width(16.dp))
    }
}