package com.manueldidonna.redhex

import android.net.Uri
import androidx.activity.invoke
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.setValue
import androidx.ui.core.Alignment
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.Text
import androidx.ui.layout.Arrangement
import androidx.ui.layout.Column
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.padding
import androidx.ui.material.Button
import androidx.ui.savedinstancestate.savedInstanceState
import androidx.ui.unit.dp
import com.manueldidonna.redhex.common.PrepareActivityContract

@Composable
fun SettingsScreen() {
    val context = ContextAmbient.current
    var launchIntent by savedInstanceState { false }
    val launcher = PrepareActivityContract(
        contractKey = "OPEN_SAVE_DATA",
        contract = ActivityResultContracts.CreateDocument(),
        activityResultCallback = ActivityResultCallback { uri: Uri? ->
            val saveData = AppState.saveData
            if (uri != null && saveData != null) {
                context.contentResolver
                    .openOutputStream(uri)
                    .use { output ->
                        output?.write(saveData.exportToBytes().toByteArray())
                    }
            }
        }
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalGravity = Alignment.CenterHorizontally
    ) {
        val padding = Modifier.padding(8.dp)
        Button(modifier = padding, onClick = { AppState.saveData = null }) {
            Text(text = "CHOOSE ANOTHER SAVEDATA")
        }
        Button(modifier = padding, onClick = { launchIntent = true }) {
            Text(text = "EXPORT SAVEDATA")
        }
    }

    if (launchIntent) {
        launchIntent = false
        launcher("")
    }

}
