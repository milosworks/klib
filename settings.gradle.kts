pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()

        maven {
            name = "Cloche"
            url = uri("https://maven.msrandom.net/repository/cloche")
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "KLib"

include(":klib")
include(":testmod")