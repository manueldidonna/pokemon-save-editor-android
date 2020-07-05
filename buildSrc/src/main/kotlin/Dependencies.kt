@file:Suppress("MemberVisibilityCanBePrivate")

object Modules {
    object Pokemon {
        const val core = ":pokemon:core"
        const val rby = ":pokemon:rby"
        const val gsc = ":pokemon:gsc"
        const val resources = ":pokemon:resources"
    }
}

object Libs {

    object Versions {
        const val kotlin = "1.3.70"
        const val compose = "0.1.0-dev14"
    }

    object Kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    }

    object KotlinX {
        private const val coroutines = "1.3.5"
        const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines"
        const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines"
    }

    object Android {
        const val materialComponents = "com.google.android.material:material:1.2.0-alpha05"
        const val timber = "com.jakewharton.timber:timber:4.7.1"
        const val coil = "io.coil-kt:coil:0.10.1"
        const val accompanistCoil = "dev.chrisbanes.accompanist:accompanist-coil:0.1.6"
    }

    object AndroidX {
        const val core = "androidx.core:core-ktx:1.1.0"
        const val activity = "androidx.activity:activity-ktx:1.2.0-alpha04"
        const val appcompat = "androidx.appcompat:appcompat:1.1.0"

        object Compose {
            const val foundation = "androidx.ui:ui-foundation:${Versions.compose}"
            const val tooling = "androidx.ui:ui-tooling:${Versions.compose}"
            const val layout = "androidx.ui:ui-layout:${Versions.compose}"
            const val material = "androidx.ui:ui-material:${Versions.compose}"
            const val savedInstanceState = "androidx.ui:ui-saved-instance-state:${Versions.compose}"
            const val materialIcons = "androidx.ui:ui-material-icons-extended:${Versions.compose}"
        }
    }
}