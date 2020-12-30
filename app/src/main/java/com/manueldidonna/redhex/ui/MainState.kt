package com.manueldidonna.redhex.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.manueldidonna.pk.core.SaveData

// TODO: find a better solution, save and restore saveData across config.changes
object MainState {
    var saveData by mutableStateOf<SaveData?>(null)
}
