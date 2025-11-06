import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea

    alias(libs.plugins.cloche)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }
}