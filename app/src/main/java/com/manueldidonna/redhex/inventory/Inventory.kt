package com.manueldidonna.redhex.inventory

import androidx.compose.*
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.zIndex
import androidx.ui.foundation.AdapterList
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.twotone.Add
import androidx.ui.text.font.FontWeight
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.manueldidonna.pk.core.*
import com.manueldidonna.pk.resources.text.PokemonTextResources
import com.manueldidonna.redhex.common.*
import com.manueldidonna.redhex.common.ui.LightColors
import com.manueldidonna.redhex.common.ui.PreviewScreen
import com.manueldidonna.redhex.common.ui.ThemedDialog
import com.manueldidonna.redhex.common.ui.translucentSurfaceColor
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlin.math.roundToInt

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
            InventoryTypes(types = saveData.supportedInventoryTypes) { type ->
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
            ItemEditor(
                item = selectedItem,
                resources = resources,
                itemIds = inventory.supportedItemIds,
                maxAllowedQuantity = inventory.maxAllowedQuantity,
                onItemChange = { item ->
                    if (inventory.getItem(item.index) != item) {
                        inventory.setItem(item)
                        items = inventory.getAllItems(spritesRetriever, resources)
                    }
                },
                onCloseRequest = onCloseRequest
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
private fun ItemEditor(
    item: Inventory.Item,
    maxAllowedQuantity: Int,
    resources: PokemonTextResources.Items,
    itemIds: List<Int>,
    onItemChange: (Inventory.Item) -> Unit,
    onCloseRequest: () -> Unit
) {
    // TODO: add a search field
    val items: List<Pair<Int, String>> = remember {
        val names = resources.getAllItems()
        itemIds.map { Pair(it, names[it]) }.sortedBy { it.second }
    }

    var quantity by state {
        item.quantity.coerceIn(1, maxAllowedQuantity)
    }

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        // show current item name
        if (item.id != 0) {
            Text(
                text = resources.getAllItems()[item.id],
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }

        Row(
            verticalGravity = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Quantity",
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            value = quantity.toFloat(),
            valueRange = 1f..maxAllowedQuantity.toFloat(),
            onValueChange = { quantity = it.roundToInt() },
            onValueChangeEnd = { onItemChange(item.toImmutable(quantity = quantity)) }
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Choose an item",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium)
        )
        AdapterList(data = items) { itemWithName ->
            ListItem(
                text = itemWithName.second,
                onClick = {
                    onItemChange(item.toImmutable(id = itemWithName.first, quantity = quantity))
                    onCloseRequest()
                }
            )
        }
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
        scrollable = false // TODO: this should be scrollable
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
    VerticalScroller {
        items.forEach { item ->
            ListItem(
                text = { Text(text = item.name) },
                secondaryText = { Text(text = "Qt. ${item.quantity}") },
                onClick = { onItemClick(item) },
                icon = { CoilImage(data = item.spriteSource.value, modifier = ItemSpriteSize) }
            )
            Divider()
        }
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

@Preview
@Composable
private fun PreviewItemEditor() {
    val fakeResources = object : PokemonTextResources.Items {
        override fun getAllItems(): List<String> = List(10) { "Master Ball" }
        override fun getTypeName(type: Inventory.Type): String = "General"
    }

    PreviewScreen(colors = LightColors) {
        ItemEditor(
            item = Inventory.Item.Immutable(0, 1, 85),
            resources = fakeResources,
            itemIds = List(10) { it },
            onItemChange = {},
            onCloseRequest = {},
            maxAllowedQuantity = 99
        )
    }
}
