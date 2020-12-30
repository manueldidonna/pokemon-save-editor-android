package com.manueldidonna.redhex.ui.pokemon

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material.icons.twotone.Face
import androidx.compose.material.icons.twotone.MenuBook
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.manueldidonna.redhex.PokemonEditorTheme
import com.manueldidonna.redhex.common.AppBarElevation
import com.manueldidonna.redhex.common.AppBarHeight
import com.manueldidonna.redhex.common.AppBarIconButton

@Composable
fun PokemonAppBar(
    modifier: Modifier = Modifier,
    storageName: String,
    showStorageList: () -> Unit,
    nextStorage: () -> Unit,
    previousStorage: () -> Unit,
    showPokedex: () -> Unit
) {
    Surface(elevation = AppBarElevation, modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(AppBarHeight)
            ) {
                Text(
                    text = storageName,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.h6
                )
                AppBarIconButton(Icons.TwoTone.MenuBook, onClick = showPokedex)
                AppBarIconButton(Icons.TwoTone.Face, onClick = {})
            }
            Divider()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(48.dp),
            ) {
                TextButton(onClick = showStorageList, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.UnfoldMore)
                    Spacer(Modifier.width(4.dp))
                    Text(text = "Show storage list")
                    Spacer(modifier = Modifier.weight(1f))
                }
                IconButton(onClick = previousStorage) {
                    Icon(Icons.Rounded.ChevronLeft, tint = MaterialTheme.colors.primary)
                }
                IconButton(onClick = nextStorage) {
                    Icon(Icons.Rounded.ChevronRight, tint = MaterialTheme.colors.primary)
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewAppBar() {
    PokemonEditorTheme {
        Surface {
            PokemonAppBar(
                storageName = "Box1",
                nextStorage = {},
                previousStorage = {},
                showStorageList = {},
                showPokedex = {}
            )
        }
    }
}