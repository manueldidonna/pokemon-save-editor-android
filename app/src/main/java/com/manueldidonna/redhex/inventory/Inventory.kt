package com.manueldidonna.redhex.inventory

import androidx.compose.foundation.Box
import androidx.compose.foundation.ContentGravity
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Add
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawShadow
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.*
import com.manueldidonna.redhex.common.*
import com.manueldidonna.redhex.common.ui.ThemedDialog
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun Inventory(modifier: Modifier, saveData: SaveData) {
    val (type, setType) = rememberMutableState { saveData.supportedInventoryTypes.first() }

    val inventory = getInventory(saveData = saveData, type = type)

    val items by remember(type) { inventory.value.items }

    // fetch all the items when the inventory change
    launchInComposition(inventory) {
        withContext(Dispatchers.IO) {
            inventory.value.fetchAllItems()
        }
    }

    val (selectedItem, selectItem) = rememberMutableState { InventoryItem.Invalid }

    Stack(modifier = modifier.fillMaxSize()) {
        Column {
            InventoryTypes(types = saveData.supportedInventoryTypes, setType)
            LazyColumnFor(items = items) { item ->
                InventoryItem(item = item, selectItem)
                Divider()
            }
        }
        if (!inventory.value.isFull)
            InsertItemButton {
                selectItem(InventoryItem.empty(index = inventory.value.size))
            }
    }

    if (selectedItem != InventoryItem.Invalid)
        InventoryEditorDialog(
            onCloseRequest = { selectItem(InventoryItem.Invalid) },
            inventory = inventory.value,
            item = selectedItem
        )
}

@Composable
private fun InventoryEditorDialog(
    onCloseRequest: () -> Unit,
    inventory: ObservableInventory,
    item: InventoryItem,
) {
    // TODO: use a bottom sheet
    val scope = rememberCoroutineScope()
    ThemedDialog(onCloseRequest = onCloseRequest) {
        InventoryEditor(
            item = item,
            itemIds = inventory.supportedItemIds,
            maxAllowedQuantity = inventory.maxQuantity,
            onItemChange = { item ->
                if (inventory.getItem(item.index) != item) {
                    scope.launch(Dispatchers.IO) {
                        inventory.stackItem(item)
                    }
                }
                onCloseRequest()
            }
        )
    }
}

@Composable
private fun StackScope.InsertItemButton(onClick: () -> Unit) {
    FloatingActionButton(
        modifier = Modifier.padding(16.dp).gravity(Alignment.BottomEnd),
        icon = { Icon(asset = Icons.TwoTone.Add) },
        onClick = onClick
    )
}

@Composable
@Stable
private fun getInventory(saveData: SaveData, type: Inventory.Type): State<ObservableInventory> {
    val resources = PokemonResourcesAmbient.current.items
    val spritesRetriever = SpritesRetrieverAmbient.current
    return rememberMutableStateFor(type) {
        ObservableInventory(saveData.getInventory(type), resources, spritesRetriever)
    }
}

@Composable
private fun InventoryTypes(types: List<Inventory.Type>, onTypeChange: (Inventory.Type) -> Unit) {
    val resources = PokemonResourcesAmbient.current.items
    var selectedIndex by rememberMutableState { 0 }
    TabRow(
        items = types,
        modifier = Modifier.fillMaxWidth().drawShadow(4.dp),
        selectedIndex = selectedIndex,
        scrollable = false
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
private fun InventoryItem(item: InventoryItem, onClick: (item: InventoryItem) -> Unit) {
    ListItem(
        text = { Text(text = item.name) },
        secondaryText = { Text(text = "Qt. ${item.quantity}") },
        onClick = { onClick(item) },
        icon = {
            Box(gravity = ContentGravity.Center, modifier = Modifier.size(40.dp)) {
                CoilImage(data = item.spriteSource.value, modifier = ItemSpriteSize)
            }
        }
    )
}
