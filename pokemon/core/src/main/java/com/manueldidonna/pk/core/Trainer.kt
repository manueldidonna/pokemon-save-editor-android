package com.manueldidonna.pk.core

data class Trainer(
    val name: String,
    val visibleId: Int,
    val secretId: Int,
    val gender: Gender,
) {
    enum class Gender {
        Male,
        Female
    }
}
