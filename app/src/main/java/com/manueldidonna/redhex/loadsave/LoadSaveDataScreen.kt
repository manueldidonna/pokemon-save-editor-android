package com.manueldidonna.redhex.loadsave

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.invoke
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ContextAmbient
import androidx.ui.tooling.preview.Preview
import com.manueldidonna.redhex.AppState
import com.manueldidonna.redhex.common.PrepareActivityContract

@Composable
fun LoadSaveDataScreen() {
    val context = ContextAmbient.current
    val launchIntent = savedInstanceState { false }
    val launcher = PrepareActivityContract(
        contractKey = "OPEN_SAVE_DATA",
        contract = ActivityResultContracts.OpenDocument(),
        activityResultCallback = ActivityResultCallback { uri: Uri? ->
            createSaveData(uri, context)
        }
    )
    Surface(color = MaterialTheme.colors.background, modifier = Modifier.fillMaxSize()) {
        Button(
            modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
            onClick = { launchIntent.value = true }
        ) {
            Text(text = "LOAD SAVEDATA")
        }
    }
    if (launchIntent.value) {
        launchIntent.value = false
        launcher(arrayOf("*/*"))
    }
}

private fun createSaveData(uri: Uri?, context: Context) {
    if (uri == null) return
    context.contentResolver.openInputStream(uri).use { input ->
        if (input == null) return
        AppState.saveData = ComposingSaveDataFactory()
            .createSaveData(input.readBytes().toUByteArray())
        Log.d("savedata", AppState.saveData.toString())
    }
}

@Preview
@Composable
private fun PreviewLoadSaveDataScreen() {
    MaterialTheme {
        LoadSaveDataScreen()
    }
}