import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.0-alpha02")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Libs.Versions.kotlin}")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xuse-experimental=kotlin.Experimental",
                "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xuse-experimental=kotlinx.coroutines.FlowPreview",
                "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes"
            )
            apiVersion = "1.3"
            jvmTarget = "1.8"
        }
    }
}
