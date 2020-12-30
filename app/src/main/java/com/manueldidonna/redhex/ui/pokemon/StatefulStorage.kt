package com.manueldidonna.redhex.ui.pokemon

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.manueldidonna.pk.core.StorageSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Stable
class StatefulStorage(private val storageSystem: StorageSystem) {
    val entries: State<List<PokemonEntity?>> = mutableStateOf(emptyList())
    suspend fun setStorageIndex() {
        withContext(Dispatchers.Default) {

        }
    }


}