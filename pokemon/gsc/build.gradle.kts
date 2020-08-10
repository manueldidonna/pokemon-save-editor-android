plugins {
    id("kotlin")
}

dependencies {
    implementation(Libs.Kotlin.stdlib)
    implementation(project(Modules.Pokemon.core))
    implementation(project(Modules.Pokemon.utils))
    implementation(project(Modules.Pokemon.resources))
}