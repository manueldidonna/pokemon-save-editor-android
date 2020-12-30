package com.manueldidonna.redhex.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    importSaveData: () -> Unit,
    exportSaveData: () -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(top = 20.dp, bottom = 16.dp)) {
        item {
            SettingsButton(text = "Choose another save data", importSaveData)
            SettingsButton(text = "Export save data", exportSaveData)
            SettingsButton(text = "View source code", onClick = { /*TODO*/ })
            SettingsButton(text = "Open source licenses", onClick = { /*TODO*/ })
            SettingsButton(text = "Choose theme", onClick = { /*TODO*/ })
        }
    }
}

@Composable
private fun SettingsButton(text: String, onClick: () -> Unit) {
    val color = MaterialTheme.colors.primary
    Text(
        text = text,
        style = MaterialTheme.typography.button,
        color = color,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick, indication = rememberRipple(color = color))
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
    )
}
