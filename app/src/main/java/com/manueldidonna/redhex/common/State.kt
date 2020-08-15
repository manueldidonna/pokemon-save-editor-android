package com.manueldidonna.redhex.common

import androidx.compose.runtime.*

@Composable
inline fun <T> rememberMutableState(
    policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy(),
    init: () -> T,
) = remember { mutableStateOf(init(), policy) }

@Composable
inline fun <T> rememberMutableStateFor(
    v1: Any,
    policy: SnapshotMutationPolicy<T> = structuralEqualityPolicy(),
    init: () -> T,
) = remember(v1) { mutableStateOf(init(), policy) }
