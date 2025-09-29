@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.cloche)

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
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

    neoforge {
        loaderVersion = libs.versions.neoforge.loader.get()

        dependencies {
            legacyClasspath(libs.kotlin.stdlib)
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
        }

        dependencies {
            fabricApi(libs.versions.fabric.api.get())

            modImplementation(libs.kotlin.fabric)
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

dependencies {
    implementation(projects.klib)
}