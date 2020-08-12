package com.manueldidonna.redhex.common

import androidx.compose.runtime.*

@Composable
inline fun <T> rememberMutableState(
    policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy(),
    init: () -> T
) = remember { mutableStateOf(init(), policy) }
