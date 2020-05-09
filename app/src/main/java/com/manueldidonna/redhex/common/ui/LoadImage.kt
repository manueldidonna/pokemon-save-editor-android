package com.manueldidonna.redhex.common.ui

import androidx.compose.*
import androidx.core.graphics.drawable.toBitmap
import androidx.ui.core.ContextAmbient
import androidx.ui.graphics.ImageAsset
import androidx.ui.graphics.asImageAsset
import coil.Coil
import coil.request.GetRequest
import coil.request.SuccessResult
import coil.size.Scale
import coil.transform.Transformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun LoadImage(
    data: Any,
    width: Int = 0,
    height: Int = 0,
    onLoad: () -> Unit = {}
): ImageAsset? {
    val context = ContextAmbient.current

    val request = remember(data, width, height) {
        GetRequest.Builder(context)
            .data(data)
            .apply {
                if (width > 0 && height > 0) {
                    size(width, height)
                    scale(Scale.FIT)
                }
            }
            .build()
    }

    var image by stateFor<ImageAsset?>(request) { null }

    // Execute the following code whenever the request changes.
    onCommit(request) {
        val job = CoroutineScope(Dispatchers.Main.immediate).launch {
            // Start loading the image and await the result.
            val result = Coil.imageLoader(context).execute(request)
            image = when (result) {
                is SuccessResult -> result.drawable.toBitmap().asImageAsset()
                else -> null
            }
            onLoad()
        }

        // Cancel the request if the input to onCommit changes or
        // the Composition is removed from the composition tree.
        onDispose { job.cancel() }
    }

    // Emit a null Image to start with.
    return image
}