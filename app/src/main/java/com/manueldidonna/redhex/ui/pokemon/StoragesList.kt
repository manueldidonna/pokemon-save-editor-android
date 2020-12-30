package com.manueldidonna.redhex.ui.pokemon

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.StorageSystem
import com.manueldidonna.redhex.PokemonEditorTheme

@Composable
fun StoragesList(
    storageSystem: StorageSystem,
    onStorageClick: (storageIndex: Int) -> Unit
) {
    val storages = remember { storageSystem.storageIndices.map(storageSystem::get) }
    LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)) {
        itemsIndexed(storages) { index, storage ->
            Storage(name = storage.name, pokemonCount = storage.size) {
                onStorageClick(storageSystem.storageIndices.first + index)
            }
        }
    }
}

@Composable
private fun Storage(name: String, pokemonCount: Int, onClick: () -> Unit) {
    val ripple = rememberRipple(color = MaterialTheme.colors.primary)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick, indication = ripple)
            .padding(horizontal = 16.dp)
    ) {
        Text(text = name, style = MaterialTheme.typography.subtitle1)
        Spacer(Modifier.weight(1f))
        Text(
            text = if (pokemonCount == 0) "Empty" else "$pokemonCount pokemon",
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary
        )
    }
}

@Preview
@Composable
private fun PreviewStorage() {
    PokemonEditorTheme {
        Surface {
            Column {
                Storage(name = "Box1", pokemonCount = 3, onClick = { /*TODO*/ })
                Storage(name = "Box2", pokemonCount = 10, onClick = { /*TODO*/ })
            }
        }
    }
}