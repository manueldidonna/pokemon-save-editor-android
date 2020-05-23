plugins {
    id("kotlin")
}

dependencies {
    implementation(Libs.Kotlin.stdlib)
    implementation(project(Modules.Pokemon.core))
}