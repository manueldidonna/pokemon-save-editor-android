package com.manueldidonna.redhex.ui.pokedex

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.twotone.ArrowBack
import androidx.compose.material.icons.twotone.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manueldidonna.redhex.DefaultIcons
import com.manueldidonna.redhex.common.AppBarElevation
import com.manueldidonna.redhex.common.AppBarHeight
import com.manueldidonna.redhex.common.AppBarIconButton
import com.manueldidonna.redhex.common.rememberMutableState

@Composable
fun PokedexAppBar(
    modifier: Modifier = Modifier,
    goBack: () -> Unit,
    filterPokemonNames: (query: String) -> Unit,
) {
    Surface(elevation = AppBarElevation, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(AppBarHeight).padding(horizontal = 4.dp),
        ) {
            IconButton(onClick = goBack) {
                Icon(DefaultIcons.ArrowBack)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Providers(AmbientContentAlpha provides ContentAlpha.disabled) {
                Text("Search...", modifier = Modifier.weight(1f))
            }
            MoreOptionsButton()
        }
    }
}

@Composable
private fun MoreOptionsButton() {
    var expandMenu by rememberMutableState(init = { false })
    // dropdown items are inserted in a card
    // TODO: find a better solution
    MaterialTheme(shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(8.dp))) {
        DropdownMenu(
            expanded = expandMenu,
            onDismissRequest = { expandMenu = false },
            toggle = {
                AppBarIconButton(icon = DefaultIcons.MoreVert, onClick = { expandMenu = true })
            },
        ) {
            DropdownMenuItem(onClick = { /*TODO*/ }) {
                Text(text = "Set all pokemon seen")
            }
            DropdownMenuItem(onClick = { /*TODO*/ }) {
                Text(text = "Set all pokemon caught")
            }
        }
    }
}
