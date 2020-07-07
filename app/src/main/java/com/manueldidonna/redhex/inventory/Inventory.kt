package com.manueldidonna.redhex.inventory

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.zIndex
import androidx.ui.foundation.Box
import androidx.ui.foundation.ContentGravity
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.Add
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.*
import com.manueldidonna.redhex.common.ui.*
import dev.chrisbanes.accompanist.coil.CoilImage

private val NullItem = Inventory.Item.empty(index = -1)

private data class InventoryItem(
    override val index: Int,
    override val id: Int,
    override val quantity: Int,
    val name: String,
    val spriteSource: SpriteSource
) : Inventory.Item

@Composable
fun Inventory(modifier: Modifier, saveData: SaveData) {
    val resources = PokemonResourcesAmbient.current.items
    val spritesRetriever = SpritesRetrieverAmbient.current

    var inventory: Inventory by state {
        saveData.getInventory(saveData.supportedInventoryTypes.first())
    }

    var items: List<InventoryItem> by state {
        inventory.getAllItems(spritesRetriever, resources)
    }

    var selectedItem: Inventory.Item by state { NullItem }

    Stack(modifier = modifier.fillMaxSize()) {
        Column {
            val toolbarTitle = remember(inventory.size) {
                "Free slots ${inventory.run { capacity - size }}/${inventory.capacity}"
            }
            InventoryToolbar(
                title = toolbarTitle,
                types = saveData.supportedInventoryTypes
            ) { type ->
                inventory = saveData.getInventory(type)
                items = inventory.getAllItems(spritesRetriever, resources)
            }
            ItemsList(items = items) { item ->
                selectedItem = item
            }
        }
        if (inventory.size < inventory.capacity)
            FloatingActionButton(
                modifier = Modifier.padding(16.dp).gravity(Alignment.BottomEnd),
                onClick = { selectedItem = Inventory.Item.empty(index = inventory.size) },
                icon = { Icon(asset = Icons.TwoTone.Add) }
            )
    }

    if (selectedItem != NullItem) {
        val onCloseRequest = { selectedItem = NullItem }
        // TODO: use a bottom sheet
        ThemedDialog(onCloseRequest = onCloseRequest) {
            InventoryItemEditor(
                item = selectedItem,
                resources = resources,
                itemIds = inventory.supportedItemIds,
                maxAllowedQuantity = inventory.maxQuantity,
                onItemChange = { item ->
                    if (inventory.getItem(item.index) != item) {
                        inventory.stackItem(item)
                        items = inventory.getAllItems(spritesRetriever, resources)
                    }
                    onCloseRequest()
                }
            )
        }
    }
}

private fun Inventory.getAllItems(
    spritesRetriever: SpritesRetriever,
    resources: PokemonTextResources.Items
): List<InventoryItem> {
    val itemNames = resources.getAllItems()
    val inventoryItem: (Int, Int, Int) -> InventoryItem = { index, id, quantity ->
        InventoryItem(index, id, quantity, itemNames[id], spritesRetriever.getItemSprite(id))
    }
    return List(size) { selectItem(it, mapTo = inventoryItem) }
}

@Composable
private fun InventoryToolbar(
    title: String,
    types: List<Inventory.Type>,
    onTypeChange: (Inventory.Type) -> Unit
) {
    Column {
        TopAppBar(
            modifier = Modifier.preferredHeight(ToolbarHeight),
            title = { Text(text = title) },
            elevation = 0.dp,
            backgroundColor = translucentSurfaceColor()
        )
        InventoryTypes(types = types, onTypeChange = onTypeChange)
    }
}

@Composable
private fun InventoryTypes(types: List<Inventory.Type>, onTypeChange: (Inventory.Type) -> Unit) {
    val resources = PokemonResourcesAmbient.current.items
    var selectedIndex by state { 0 }
    TabRow(
        backgroundColor = translucentSurfaceColor(),
        items = types,
        modifier = Modifier.zIndex(8f).fillMaxWidth(),
        selectedIndex = selectedIndex,
        scrollable = true // TODO: this should be scrollable
    ) { index, type ->
        Tab(
            modifier = Modifier.preferredHeight(48.dp),
            text = { Text(text = resources.getTypeName(type)) },
            selected = index == selectedIndex,
            onSelected = {
                onTypeChange(type)
                selectedIndex = index
            }
        )
    }
}

@Composable
private fun ItemsList(items: List<InventoryItem>, onItemClick: (item: Inventory.Item) -> Unit) {
    LazyColumnItems(items = items) { item ->
        ListItem(
            text = { Text(text = item.name) },
            secondaryText = { Text(text = "Qt. ${item.quantity}") },
            onClick = { onItemClick(item) },
            icon = {
                Box(gravity = ContentGravity.Center, modifier = Modifier.size(40.dp)) {
                    CoilImage(data = item.spriteSource.value, modifier = ItemSpriteSize)
                }
            }
        )
        Divider()
    }
}

@Preview
@Composable
private fun PreviewItemsList() {
    PreviewScreen(colors = LightColors) {
        ItemsList(
            items = List(3) { InventoryItem(it, 0, 99, "Master Ball", SpriteSource("")) },
            onItemClick = {}
        )
    }
}
