package com.manueldidonna.redhex.details

import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.manueldidonna.pk.core.Pokemon
import com.manueldidonna.pk.core.Version
import com.manueldidonna.pk.resources.unownLetters
import com.manueldidonna.redhex.common.rememberMutableState
import com.manueldidonna.redhex.common.ui.RadioButtonWithText
import com.manueldidonna.redhex.common.ui.ThemedDialog

@Composable
fun ModifyForm(
    version: Version,
    form: ObservablePokemon.FormWrapper,
    onFormChange: (Pokemon.Form) -> Unit,
) {
    val unwrappedForm = form.form ?: return
    val value = when (unwrappedForm) {
        is Pokemon.Form.Unown -> "Letter ${unwrappedForm.letter.toUpperCase()}"
    }
    var changeForm by rememberMutableState { false }
    LabelledValue(
        label = "Form",
        value = value,
        modifier = Modifier.clickable(onClick = { changeForm = true }).padding(horizontal = 16.dp)
    )
    if (changeForm) {
        ChangeUnownLetterDialog(
            version = version,
            onCloseRequest = { changeForm = false },
            selectedLetter = unwrappedForm.letter,
            onLetterChange = {
                onFormChange(Pokemon.Form.Unown(letter = it))
            }
        )
    }
    if (changeForm)
        ChangeUnownLetterDialog(
            version = version,
            onCloseRequest = { changeForm = false },
            selectedLetter = unwrappedForm.letter,
            onLetterChange = {
                onFormChange(Pokemon.Form.Unown(letter = it))
            }
        )
}

@Composable
private fun ChangeUnownLetterDialog(
    version: Version,
    onCloseRequest: () -> Unit,
    selectedLetter: Char,
    onLetterChange: (Char) -> Unit,
) {
    val unownLetters = unownLetters(version)
    ThemedDialog(onCloseRequest = onCloseRequest) {
        Column {
            Text(
                text = "Unown Letter",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumnFor(items = unownLetters) { letter ->
                RadioButtonWithText(
                    text = letter.toString(),
                    selected = letter == selectedLetter,
                    onClick = {
                        onLetterChange(letter)
                        onCloseRequest()
                    }
                )
            }
        }
    }
}
