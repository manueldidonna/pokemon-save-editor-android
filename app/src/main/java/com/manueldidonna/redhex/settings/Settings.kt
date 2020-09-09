package com.manueldidonna.redhex.settings

import android.net.Uri
import androidx.activity.invoke
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material.icons.twotone.Face
import androidx.compose.material.icons.twotone.SaveAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.redhex.common.LabelledValue
import com.manueldidonna.redhex.common.PrepareActivityContract
import com.manueldidonna.redhex.common.ThemedDialog
import com.manueldidonna.redhex.common.rememberMutableState

@Composable
fun Settings(saveData: SaveData, removeSaveData: () -> Unit) {
    val context = ContextAmbient.current
    var launchIntent by savedInstanceState { false }
    val launcher = PrepareActivityContract(
        contractKey = "EXPORT_SAVE_DATA",
        contract = ActivityResultContracts.CreateDocument(),
        activityResultCallback = { uri: Uri? ->
            if (uri != null) {
                context.contentResolver
                    .openOutputStream(uri)
                    .use { output ->
                        output?.write(saveData.exportToBytes().toByteArray())
                    }
            }
        }
    )
    if (launchIntent) {
        launchIntent = false
        launcher("")
    }
    var showTrainerInfo by rememberMutableState { false }
    if (showTrainerInfo) {
        ThemedDialog(onDismissRequest = { showTrainerInfo = false }) {
            with(saveData.trainer) {
                TrainerInfo(name = name, visibleId = visibleId, secretId = secretId)
            }
        }
    }
    ScrollableColumn(contentPadding = InnerPadding(top = 16.dp, bottom = 16.dp)) {
        val trainer = remember { saveData.trainer }
        LabelledValue(
            label = "Game Version",
            // TODO: support localization
            value = saveData.version.name,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Divider()
        LabelWithIcon(
            icon = Icons.TwoTone.Face,
            label = "Show Trainer",
            modifier = Modifier
                .clickable(onClick = { showTrainerInfo = true })
                .padding(horizontal = 24.dp)
        )
        Divider()
        LabelWithIcon(
            icon = Icons.TwoTone.SaveAlt,
            label = "Export Save Data",
            modifier = Modifier
                .clickable(onClick = { launchIntent = true })
                .padding(horizontal = 24.dp)
        )
        LabelWithIcon(
            icon = Icons.TwoTone.Close,
            label = "Remove Save Data",
            modifier = Modifier
                .clickable(onClick = removeSaveData)
                .padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun TrainerInfo(name: String, visibleId: Int, secretId: Int) {
    Column {
        LabelledValue(
            label = "Trainer Name",
            value = name,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Divider()
        LabelledValue(
            label = "Visible ID",
            value = visibleId.toString(),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        LabelledValue(
            label = "Secret ID",
            value = secretId.toString(),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun LabelWithIcon(icon: VectorAsset, label: String, modifier: Modifier = Modifier) {
    Row(
        verticalGravity = Alignment.CenterVertically,
        modifier = modifier.preferredHeight(56.dp).fillMaxWidth()
    ) {
        Icon(asset = icon)
        Spacer(Modifier.width(24.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.subtitle2,
            fontWeight = FontWeight.Medium,
        )
    }
}
