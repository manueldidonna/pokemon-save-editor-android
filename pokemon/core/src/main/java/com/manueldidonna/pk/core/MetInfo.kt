package com.manueldidonna.pk.core

data class MetInfo(
    val level: Int,
    val time: Time,
    val locationId: Int,
    val trainerGender: Trainer.Gender
    // TODO: val ballId: Int
) {

    // TODO: data class Date(val time: OffsetDateTime): Time()
    sealed class Time {
        /**
         * Accepted values for [value] are [Morning], [Day] and [Night]
         */
        data class TimesOfDay(val value: Int) : Time() {
            companion object {
                const val Morning = 1
                const val Day = 2
                const val Night = 3
            }
        }
    }
}
