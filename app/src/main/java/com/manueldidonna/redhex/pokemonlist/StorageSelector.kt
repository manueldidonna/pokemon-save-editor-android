package com.manueldidonna.redhex.pokemonlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.StorageSystem
import com.manueldidonna.redhex.common.LabelledValue

@Composable
fun StorageSelector(
    modifier: Modifier = Modifier,
    storageSystem: StorageSystem,
    onSelect: (storageIndex: Int) -> Unit,
) {
    val firstIndex = storageSystem.storageIndices.first
    val storageList = storageSystem.storageIndices.map { index -> storageSystem[index] }
    LazyColumnForIndexed(modifier = modifier, items = storageList) { index, storage ->
        LabelledValue(
            modifier = Modifier
                .clickable(onClick = { onSelect(index + firstIndex) })
                .padding(horizontal = 16.dp),
            label = storage.name,
            value = getStorageSize(storage.size)
        )
    }
}

@Stable
private fun getStorageSize(size: Int): String {
    return if (size == 0) "Empty" else "$size pokemon"
}
