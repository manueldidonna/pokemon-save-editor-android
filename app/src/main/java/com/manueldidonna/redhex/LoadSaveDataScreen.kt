package com.manueldidonna.redhex

import android.content.Context
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
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.wrapContentSize
import androidx.ui.material.MaterialTheme
import androidx.ui.material.OutlinedButton
import androidx.ui.material.Surface
import androidx.ui.savedinstancestate.savedInstanceState
import androidx.ui.tooling.preview.Preview
import com.manueldidonna.pk.rby.RBYSaveDataFactory
import com.manueldidonna.redhex.common.PrepareActivityContract

@Composable
fun LoadSaveDataScreen() {
    val context = ContextAmbient.current
    var launchIntent by savedInstanceState { false }
    val launcher = PrepareActivityContract(
        contractKey = "OPEN_SAVE_DATA",
        contract = ActivityResultContracts.OpenDocument(),
        activityResultCallback = ActivityResultCallback { uri: Uri? ->
            createSaveData(uri, context)
        }
    )
    Surface(color = MaterialTheme.colors.background, modifier = Modifier.fillMaxSize()) {
        OutlinedButton(
            modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
            onClick = { launchIntent = true }
        ) {
            Text(text = "LOAD SAVEDATA")
        }
    }
    if (launchIntent) {
        launchIntent = false
        launcher(arrayOf("*/*"))
    }
}

private fun createSaveData(uri: Uri?, context: Context) {
    if (uri == null) return
    context.contentResolver.openInputStream(uri).use { input ->
        if (input == null) return
        val saveData = RBYSaveDataFactory
            .createSaveData(input.readBytes().toUByteArray())
            ?: return
        MainState.saveData = saveData
    }
}

@Preview
@Composable
private fun PreviewLoadSaveDataScreen() {
    MaterialTheme {
        LoadSaveDataScreen()
    }
}