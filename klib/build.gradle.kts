@file:Suppress("UnstableApiUsage")

plugins {
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

        modId = id
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
            compileOnlyApi(libs.kotlinx.serialization.cbor)
            compileOnlyApi(compose.dependencies.runtime)
        }
    }

    neoforge {
        loaderVersion = libs.versions.neoforge.loader.get()

        metadata {
            dependency("kotlinforforge", libs.versions.kotlin.neoforge.get())
        }

        dependencies {
            legacyClasspath(libs.kotlinx.serialization.cbor)
            legacyClasspath(compose.dependencies.runtime)
        }
    }

    fabric {
        loaderVersion = libs.versions.fabric.loader.get()

        includedClient()

        metadata {
            entrypoint("main") {
                value = "xyz.milosworks.klib.fabric.KLibFabric"
                adapter = "kotlin"
            }

            dependency("fabric-language-kotlin", libs.versions.kotlin.fabric.get())
        }

        dependencies {
            fabricApi(libs.versions.fabric.api.get())

            // This contains cbor
            modImplementation(libs.kotlin.fabric)

            implementation(compose.dependencies.runtime)
        }
    }

    targets.all {
        runs {
            client()
        }
    }
}

kotlin {
    jvmToolchain(21)
}