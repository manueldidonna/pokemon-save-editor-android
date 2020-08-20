package com.manueldidonna.redhex

import android.net.Uri
import androidx.activity.invoke
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.unit.dp
import com.manueldidonna.redhex.common.PrepareActivityContract

@Composable
fun SettingsScreen() {
    val context = ContextAmbient.current
    val launchIntent = savedInstanceState { false }
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
            Text(text = "CHOOSE ANOTHER SAVE DATA")
        }
        Button(modifier = padding, onClick = { launchIntent.value = true }) {
            Text(text = "EXPORT SAVE DATA")
        }
    }

    if (launchIntent.value) {
        launchIntent.value = false
        launcher("")
    }

}
