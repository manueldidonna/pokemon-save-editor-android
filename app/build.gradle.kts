import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(Config.compileSdkVersion)
    defaultConfig {
        minSdkVersion(Config.minSdkVersion)
        targetSdkVersion(Config.targetSdkVersion)
        versionCode = Config.versionCode
        versionName = Config.versionName
        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    composeOptions {
        kotlinCompilerVersion = "1.4.0-rc"
        kotlinCompilerExtensionVersion = Libs.Versions.compose
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(Modules.Pokemon.core))
    implementation(project(Modules.Pokemon.rby))
    implementation(project(Modules.Pokemon.gsc))
    implementation(project(Modules.Pokemon.resources))
    implementation(Libs.Kotlin.stdlib)
    implementation(Libs.AndroidX.core)
    implementation(Libs.AndroidX.appcompat)
    implementation(Libs.AndroidX.activity)
    implementation(Libs.AndroidX.Compose.foundation)
    implementation(Libs.AndroidX.Compose.tooling)
    implementation(Libs.AndroidX.Compose.material)
    implementation(Libs.AndroidX.Compose.Foundation.layout)
    implementation(Libs.AndroidX.Compose.Runtime.savedState)
    implementation(Libs.AndroidX.Compose.Material.icons)
    implementation(Libs.Android.materialComponents)
    implementation(Libs.KotlinX.coroutinesAndroid)
    implementation(Libs.Android.coil)
    implementation(Libs.Android.accompanistCoil)
}
