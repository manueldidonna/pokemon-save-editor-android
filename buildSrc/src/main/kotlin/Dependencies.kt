@file:Suppress("MemberVisibilityCanBePrivate")

import Libs.Versions.compose

object Modules {
    object Pokemon {
        const val core = ":pokemon:core"
        const val rby = ":pokemon:rby"
        const val gsc = ":pokemon:gsc"
        const val utils = ":pokemon:utils"
        const val resources = ":pokemon:resources"
    }
}

object Libs {

    object Versions {
        const val kotlin = "1.4.21"
        const val compose = "1.0.0-alpha09"
    }

    object KotlinX {
        private const val coroutines = "1.3.9"
        const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines"
        const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines"
    }

    object Android {
        const val materialComponents = "com.google.android.material:material:1.2.0"
        const val timber = "com.jakewharton.timber:timber:4.7.1"
        const val coil = "io.coil-kt:coil:1.0.0-rc1"
    }

    object AndroidX {
        const val core = "androidx.core:core-ktx:1.5.0-alpha05"
        const val activity = "androidx.activity:activity-ktx:1.2.0-rc01"
        const val appcompat = "androidx.appcompat:appcompat:1.3.0-alpha02"

        object Compose {
            const val foundation = "androidx.compose.foundation:foundation:$compose"
            const val tooling = "androidx.compose.ui:ui-tooling:$compose"
            const val layout = "androidx.compose.foundation:foundation-layout:$compose"
            const val savedState = "androidx.compose.runtime:runtime-saved-instance-state:$compose"
            const val material = "androidx.compose.material:material:$compose"
            const val icons = "androidx.compose.material:material-icons-extended:$compose"
            // accompanist
            const val insets = "dev.chrisbanes.accompanist:accompanist-insets:0.4.1"
            const val coil = "dev.chrisbanes.accompanist:accompanist-coil:0.4.1"
        }
    }
}