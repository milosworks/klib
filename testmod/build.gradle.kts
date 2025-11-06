@file:Suppress("UnstableApiUsage")

import net.msrandom.stubs.GenerateStubApi
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    idea

    alias(libs.plugins.cloche)

    kotlin("jvm")

    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.compose)
}

repositories {
    cloche.librariesMinecraft()

    mavenCentral()
    google()

    maven("https://maven.terraformersmc.com/") { name = "Terraformers" }
    maven("https://thedarkcolour.github.io/KotlinForForge/") { name = "KFF" }
    maven("https://maven.architectury.dev/") { name = "Architectury " }

    cloche {
        main()

        mavenNeoforged()
        mavenNeoforgedMeta()
        mavenFabric()

        mavenParchment()
    }
}

cloche {
    metadata {
        val id: String by project
        val name: String by project
        val license: String by project
        val description: String by project
        val authors: String by project
        val contributors: String by project

        modId = "${id}_test"
        this.name = name
        this.license = license
        this.description = description

        authors.split(',').map { it.trim() }.forEach { author(it) }
        contributors.split(',').map { it.trim() }.forEach { contributor(it) }
    }

    minecraftVersion = libs.versions.minecraft.get()

    mappings {
        official()
        parchment(libs.versions.parchment.get())
    }

    common {
        dependencies {
            compileOnly(libs.kotlin.stdlib)
            compileOnly(libs.kotlinx.serialization.asProvider())

            compileOnlyApi(libs.kotlinx.serialization.json)
            compileOnlyApi(libs.kotlinx.serialization.cbor)

            api(libs.kotlinx.serialization.nbt)

            api(compose.dependencies.runtime)

            modImplementation(projects.klib)
        }
    }

    neoforge {
        loaderVersion = libs.versions.neoforge.loader.get()

        dependencies {
            modImplementation(stripIncludes(libs.kotlin.neoforge))
            modImplementation(libs.architectury.neoforge)

            legacyClasspath(dependencies.kotlin("reflect").toString())

            legacyClasspath(libs.kotlinx.serialization.json)
            legacyClasspath(libs.kotlinx.serialization.cbor)
            legacyClasspath(libs.kotlinx.serialization.nbt)

            legacyClasspath(compose.dependencies.runtime)
        }
    }

    fabric {
        loaderVersion = libs.versions.fabric.loader.get()

        includedClient()

        metadata {
            entrypoint("main") {
                value = "xyz.milosworks.ktest.fabric.KTestFabric"
                adapter = "kotlin"
            }
            entrypoint("client") {
                value = "xyz.milosworks.ktest.fabric.KTestFabric"
                adapter = "kotlin"
            }
        }

        dependencies {
            fabricApi(libs.versions.fabric.api.get())
            modImplementation(libs.kotlin.fabric)
            modImplementation(libs.architectury.fabric)

            modImplementation("com.terraformersmc:modmenu:11.0.3")
        }
    }

    targets.all {
        runs {
            client {
                arguments.addAll("--username", "Vyrek_", "--quickPlaySingleplayer", "Test")
            }
        }
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.named<GenerateStubApi>("createCommonApiStub") {
    excludes.add(projects.klib.group!!)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
    }
}