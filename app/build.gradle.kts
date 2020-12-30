import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdkVersion(30)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 1
        versionName =  "0.1"
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
        kotlinCompilerVersion = "1.4.21"
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
    implementation(Libs.Android.timber)
    implementation(Libs.AndroidX.core)
    implementation(Libs.AndroidX.appcompat)
    implementation(Libs.AndroidX.activity)
    implementation(Libs.AndroidX.Compose.foundation)
    implementation(Libs.AndroidX.Compose.material)
    implementation(Libs.AndroidX.Compose.layout)
    implementation(Libs.AndroidX.Compose.savedState)
    implementation(Libs.AndroidX.Compose.icons)
    implementation(Libs.Android.materialComponents)
    implementation(Libs.KotlinX.coroutinesAndroid)
    implementation(Libs.Android.coil)
    implementation(Libs.AndroidX.Compose.coil)
    implementation(Libs.AndroidX.Compose.insets)
    implementation(Libs.AndroidX.Compose.tooling)
}
