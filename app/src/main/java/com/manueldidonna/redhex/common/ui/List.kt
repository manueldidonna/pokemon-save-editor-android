package com.manueldidonna.redhex.common.ui

import androidx.compose.*
import androidx.ui.core.Modifier
import androidx.ui.core.onPositioned
import androidx.ui.foundation.Box
import androidx.ui.foundation.ScrollerPosition
import androidx.ui.foundation.VerticalScroller
import androidx.ui.layout.Column

private const val threshold = 200f

// TODO: Remove this composable when AdapterList became less bugged.
//  Vertical List has really bad performance
//  but still better than a simple vertical scroller with column
@Composable
fun <T : Any> VerticalList(
    modifier: Modifier = Modifier,
    data: List<T>,
    itemCallback: @Composable() (T) -> Unit
) {
    val scrollerPosition = ScrollerPosition()
    var firstVisibleItem by stateFor(data) { 0 }
    var lastVisibleItem by stateFor(data) { 0 }
    var height by stateFor(data) { 0f }
    var childrenHeight by stateFor(data) { 0f }
    val itemHeightArray = remember(data) { FloatArray(data.size) { 0f } }
    VerticalScroller(
        modifier = modifier.onPositioned {
            height = it.size.height.value.toFloat()
        },
        scrollerPosition = scrollerPosition
    ) {
        if (data.isNotEmpty()) {
            Column(Modifier.onPositioned {
                childrenHeight = it.size.height.value.toFloat()
                val scroll = scrollerPosition.value
                val firstHeight = itemHeightArray[firstVisibleItem]
                val lastHeight = itemHeightArray[lastVisibleItem]
                // check if we must decrease the first visible item
                if (scroll <= threshold && firstVisibleItem > 0) { // add item in front of the list
                    firstVisibleItem--
                    scrollerPosition.scrollBy(itemHeightArray[firstVisibleItem]) // scroll height of added item
                }
                // check if we can increase the first visible item
                if (scroll > firstHeight + lastHeight + threshold) { // remove invisible item
                    firstVisibleItem++
                    scrollerPosition.scrollBy(-firstHeight) // scroll height of removed item
                }
                // check if we must increase last visible item
                val pixelsInvisible = childrenHeight - height - scroll // invisible pixels left
                if (pixelsInvisible <= threshold && lastVisibleItem < data.size - 1) {
                    lastVisibleItem++
                }
                // check if we can decrease the last visible item
                if (pixelsInvisible > lastHeight + firstHeight + threshold && lastVisibleItem > 0) {
                    lastVisibleItem--
                }
            }) {
                for (i in firstVisibleItem..lastVisibleItem) {
                    VerticalListItem(item = data[i], onPositioned = { height ->
                        itemHeightArray[i] = height.toFloat() // remember height
                    }, itemCallback = itemCallback)
                }
            }
        }
    }
}

@Composable
private inline fun <T : Any> VerticalListItem(
    @Pivotal item: T,
    crossinline onPositioned: (Int) -> Unit,
    crossinline itemCallback: @Composable() (T) -> Unit
) {
    Box(
        modifier = Modifier.onPositioned { onPositioned(it.size.height.value) },
        children = { itemCallback(item) }
    )
}