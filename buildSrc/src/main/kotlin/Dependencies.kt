@file:Suppress("MemberVisibilityCanBePrivate")

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
        const val kotlin = "1.4.0"
        const val compose = "0.1.0-dev17"
    }

    object Kotlin {
        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
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
        const val accompanistCoil = "dev.chrisbanes.accompanist:accompanist-coil:0.1.9"
    }

    object AndroidX {
        const val core = "androidx.core:core-ktx:1.1.0"
        const val activity = "androidx.activity:activity-ktx:1.2.0-alpha04"
        const val appcompat = "androidx.appcompat:appcompat:1.1.0"

        object Compose {
            const val foundation = "${Foundation.artifact}:foundation:${Versions.compose}"
            const val material = "${Material.artifact}:material:${Versions.compose}"
            const val tooling = "androidx.ui:ui-tooling:${Versions.compose}"

            object Foundation {
                internal const val artifact = "androidx.compose.foundation"
                const val layout = "$artifact:foundation-layout:${Versions.compose}"
            }

            object Runtime {
                private const val artifact = "androidx.compose.runtime"
                const val savedState = "$artifact:runtime-saved-instance-state:${Versions.compose}"
            }

            object Material {
                internal const val artifact = "androidx.compose.material"
                const val icons = "$artifact:material-icons-extended:${Versions.compose}"
            }
        }
    }
}