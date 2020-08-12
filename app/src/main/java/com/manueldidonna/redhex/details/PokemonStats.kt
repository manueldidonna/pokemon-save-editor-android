package com.manueldidonna.redhex.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import com.manueldidonna.pk.core.Pokemon

private class ObservableStatisticValues(stats: Pokemon.StatisticValues) : Pokemon.StatisticValues {
    override var health by mutableStateOf(stats.health, structuralEqualityPolicy())
    override var attack by mutableStateOf(stats.attack, structuralEqualityPolicy())
    override var defense by mutableStateOf(stats.defense, structuralEqualityPolicy())
    override var specialAttack by mutableStateOf(stats.specialAttack, structuralEqualityPolicy())
    override var specialDefense by mutableStateOf(stats.specialDefense, structuralEqualityPolicy())
    override var speed by mutableStateOf(stats.speed, structuralEqualityPolicy())
}
