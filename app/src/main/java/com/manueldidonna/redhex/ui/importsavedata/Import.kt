package com.manueldidonna.redhex.ui.importsavedata

import android.content.Context
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AmbientContext
import com.manueldidonna.pk.core.SaveData
import com.manueldidonna.redhex.common.registerForActivityResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImportSaveDataScreen(onImport: (SaveData?) -> Unit) {
    val context = AmbientContext.current
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    val launcher = registerForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            val saveData = createSaveData(uri, context)?.let(onImport)
            if (saveData == null)
                coroutineScope.launch {
                    scaffoldState
                        .snackbarHostState
                        .showSnackbar(message = "Unsupported File")
                }
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState
    ) {
        Button(
            modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
            onClick = { launcher.launch("*/*") }
        ) {
            Text(text = "IMPORT DATA FROM DISK")
        }
    }
}

private fun createSaveData(uri: Uri, context: Context): SaveData? {
    context.contentResolver.openInputStream(uri).use { input ->
        if (input == null) return null
        return SaveDataFactory.create(input.readBytes().toUByteArray())
    }
}
