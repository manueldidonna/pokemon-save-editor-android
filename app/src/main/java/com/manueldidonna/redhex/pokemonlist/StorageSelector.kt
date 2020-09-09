package com.manueldidonna.redhex.pokemonlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Storage
import com.manueldidonna.pk.core.StorageSystem
import com.manueldidonna.redhex.common.LabelledValue
import com.manueldidonna.redhex.common.ThemedDialog
import com.manueldidonna.redhex.common.rememberMutableState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun StorageSelectorDialog(
    modifier: Modifier = Modifier,
    storageSystem: StorageSystem,
    onSelect: (storageIndex: Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val firstIndex = storageSystem.storageIndices.first
    var storageList by rememberMutableState { emptyList<Storage>() }
    launchInComposition {
        withContext(Dispatchers.Default) {
            storageList = storageSystem
                .storageIndices
                .map { index -> storageSystem[index] }
        }
    }
    if (storageList.isEmpty()) return
    ThemedDialog(onDismissRequest = onDismissRequest) {
        LazyColumnForIndexed(modifier = modifier, items = storageList) { index, storage ->
            LabelledValue(
                modifier = Modifier
                    .clickable(onClick = {
                        onSelect(index + firstIndex)
                        onDismissRequest()
                    })
                    .padding(horizontal = 16.dp),
                label = storage.name,
                value = getStorageSize(storage.size)
            )
        }
    }
}

@Stable
private fun getStorageSize(size: Int): String {
    return if (size == 0) "Empty" else "$size pokemon"
}
