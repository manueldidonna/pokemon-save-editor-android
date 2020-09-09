package com.manueldidonna.redhex.settings

import android.content.Context
import android.net.Uri
import androidx.activity.invoke
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
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.redhex.common.PrepareActivityContract

@Composable
fun ImportSaveDataFromFile(onImportFinish: (SaveData?) -> Unit) {
    val context = ContextAmbient.current
    val launchIntent = savedInstanceState { false }
    val launcher = PrepareActivityContract(
        contractKey = "OPEN_SAVE_DATA",
        contract = ActivityResultContracts.OpenDocument(),
        activityResultCallback = { uri: Uri? ->
            onImportFinish(createSaveData(uri, context))
        }
    )
    Surface(color = MaterialTheme.colors.background, modifier = Modifier.fillMaxSize()) {
        Button(
            modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
            onClick = { launchIntent.value = true }
        ) {
            Text(text = "IMPORT SAVE DATA")
        }
    }
    if (launchIntent.value) {
        launchIntent.value = false
        launcher(arrayOf("*/*"))
    }
}

private fun createSaveData(uri: Uri?, context: Context): SaveData? {
    if (uri == null) return null
    context.contentResolver.openInputStream(uri).use { input ->
        if (input == null) return null
        return SaveDataFactory.create(input.readBytes().toUByteArray())
    }
}
