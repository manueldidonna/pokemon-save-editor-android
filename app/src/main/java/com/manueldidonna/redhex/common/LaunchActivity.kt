package com.manueldidonna.redhex.common

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.Composable
import androidx.compose.onDispose
import androidx.compose.remember
import androidx.ui.core.LifecycleOwnerAmbient

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun <I, O> PrepareActivityContract(
    contractKey: String,
    contract: ActivityResultContract<I, O>,
    activityResultCallback: ActivityResultCallback<O>
): ActivityResultLauncher<I> {
    val lifecycleOwner = LifecycleOwnerAmbient.current
    val registry = ActivityResultRegistryAmbient.current

    val launcher: ActivityResultLauncher<I> = remember(contract, registry) {
        registry.register(contractKey, lifecycleOwner, contract, activityResultCallback)
    }
    onDispose {
        launcher.unregister()
    }
    return launcher
}