package com.manueldidonna.redhex.ui.bag

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FloatingActionButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Bag
import com.manueldidonna.pk.core.isEmpty
import com.manueldidonna.pk.core.isFull
import com.manueldidonna.pk.core.stackItem
import com.manueldidonna.redhex.common.rememberMutableState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BagScreen(bag: Bag) {
    val inventoryTypes = remember { bag.inventoryTypes.toList() }

    var selectedTpe by rememberMutableState { inventoryTypes.first() }

    val inventory = rememberStatefulInventory(bag, selectedTpe)

    var selectedItemIndex by rememberMutableState<Int?> { null }

    if (selectedItemIndex != null) {
        val index = selectedItemIndex!!
        BagItemEditorDialog(
            allItemIds = inventory.supportedItemIds,
            quantityRange = 1..inventory.maxQuantity,
            itemToEdit = inventory.items.getOrNull(index),
            onSelectionChange = { item ->
                inventory.stackItem(index, item)
                selectedItemIndex = null
            },
            deleteSelection = {
                inventory.removeItemAt(index)
                selectedItemIndex = null
            }
        )
    }

    Box {
        val listState = rememberLazyListState()
        Column {
            BagAppBar(
                modifier = Modifier.wrapContentHeight(),
                inventoryTypes = inventoryTypes,
                selectedType = selectedTpe,
                onTypeChange = { selectedTpe = it },
                filterItems = { /*TODO*/ }
            )
            BagItemsList(
                listState = listState,
                inventoryItems = inventory.items,
                inventoryCapacity = inventory.capacity,
                contentPadding = remember(inventory.isFull) {
                    val bottomPadding = if (inventory.isFull) 16.dp else 88.dp
                    PaddingValues(top = 16.dp, bottom = bottomPadding)
                },
                onItemClick = { index -> selectedItemIndex = index }
            )
            LaunchedEffect(subject = selectedTpe) {
                if (!inventory.isEmpty) listState.snapToItemIndex(index = 0)
            }
        }
        val isScrollingForward by listState.isScrollingForward()
        InsertItemButton(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = !inventory.isFull && !isScrollingForward,
            onClick = { selectedItemIndex = inventory.size }
        )
    }
}

private data class ScrollInfoHolder(var index: Int = 0, var scrollOffset: Int = 0)

@Composable
private fun LazyListState.isScrollingForward(): State<Boolean> {
    val holder = remember(this) { ScrollInfoHolder() }
    val scrollOffset = firstVisibleItemScrollOffset
    val index = firstVisibleItemIndex
    return derivedStateOf {
        val isForward =
            if (holder.index != index) holder.index < index
            else holder.scrollOffset < scrollOffset
        holder.scrollOffset = scrollOffset
        holder.index = index
        return@derivedStateOf isForward
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun InsertItemButton(
    modifier: Modifier,
    visible: Boolean,
    onClick: () -> Unit,
) {
    val transition = updateTransition(targetState = visible)

    val alpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 220, easing = FastOutSlowInEasing) },
        targetValueByState = { state -> if (state) 1f else 0f }
    )

    val translationY by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 220, easing = FastOutSlowInEasing) },
        targetValueByState = { state ->
            if (state) 0f else with(AmbientDensity.current) { 80.dp.toPx() / 3 * 2 }
        }
    )

    Box(modifier = modifier.graphicsLayer {
        this.alpha = alpha
        this.translationY = translationY
    }) {
        ExtendedFloatingActionButton(
            text = { Text("INSERT ITEM") },
            onClick = onClick,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 2.dp,
                pressedElevation = 6.dp
            ),
            modifier = Modifier.padding(24.dp).fillMaxWidth()
        )
    }
}
