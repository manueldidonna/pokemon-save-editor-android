package com.manueldidonna.redhex.common.ui

import androidx.compose.Composable
import androidx.compose.remember
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.layout.*
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ProvideEmphasis
import androidx.ui.material.Surface
import androidx.ui.material.ripple.ripple
import androidx.ui.unit.dp

class DialogScope(val dismiss: () -> Unit) {
    inline fun dismissAfterAction(crossinline action: () -> Unit): () -> Unit = {
        action()
        dismiss()
    }
}

@Composable
fun DialogMenu(dismiss: () -> Unit, content: @Composable() DialogScope.() -> Unit) {
    val currentColors = MaterialTheme.colors
    val currentTypography = MaterialTheme.typography
    Dialog(onCloseRequest = dismiss) {
        MaterialTheme(colors = currentColors, typography = currentTypography) {
            Surface(
                modifier = Modifier.preferredWidth(280.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = 2.dp
            ) {
                val scope = remember(dismiss) {
                    DialogScope(
                        dismiss
                    )
                }
                Column(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)) {
                    scope.content()
                }
            }
        }
    }
}

@Composable
inline fun DialogScope.DialogItem(text: String, crossinline onClick: () -> Unit) {
    Clickable(onClick = dismissAfterAction(onClick), modifier = Modifier.ripple()) {
        ProvideEmphasis(EmphasisAmbient.current.high) {
            Box(
                modifier = Modifier.preferredHeight(56.dp).fillMaxWidth(),
                gravity = ContentGravity.CenterStart,
                paddingStart = 16.dp,
                paddingEnd = 16.dp
            ) {
                Text(text = text, style = MaterialTheme.typography.subtitle1)
            }
        }
    }
}