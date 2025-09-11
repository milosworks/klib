import net.kernelpanicsoft.archie.plugin.bundleRuntimeLibrary

plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.archie)
}

val String.prop: String?
    get() = rootProject.properties[this] as String?

val String.env: String?
    get() = System.getenv(this)

val modVersion = ("TAG".env ?: "mod_version".prop)!!

architectury {
    platformSetupLoomIde()
    fabric()
}

@Suppress("UnstableApiUsage")
configurations {
    create("common")
    create("shadowCommon")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    testCompileClasspath.get().extendsFrom(compileClasspath.get())
    testRuntimeClasspath.get().extendsFrom(runtimeClasspath.get())
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    mods {
        maybeCreate("main").apply {
            sourceSet(project.sourceSets.main.get())
            sourceSet(project(":common").sourceSets.main.get())
        }
        create("test") {
            sourceSet(project.sourceSets.test.get())
            sourceSet(project(":common").sourceSets.test.get())
        }
    }

    runs {
        getByName("client") {
            source(sourceSets.test.get())

//            System.setProperty("org.slf4j.simpleLogger.defaullltLogLevel", "debug")

            programArgs("--username", "Vyrek_", "--quickPlaySingleplayer", "Test")
        }
    }
}

//sourceSets {
//	main {
//		resources {
//		}
//		kotlin {
//			srcDir("src/main/gametest")
//		}
//		java {
//			srcDir("src/main/mixin")
//		}
//	}
//}

dependencies {
    modImplementation(libs.fabric.loader)
    modApi(libs.fabric.api)
    modApi(libs.architectury.fabric)
    modImplementation(libs.kotlin.fabric)

    bundleRuntimeLibrary(libs.kotlinx.serialization.nbt)
    bundleRuntimeLibrary(libs.kotlinx.serialization.toml)
    bundleRuntimeLibrary(libs.kotlinx.serialization.json5)
    bundleRuntimeLibrary(libs.kotlinx.serialization.cbor)

    bundleRuntimeLibrary(compose.runtime)

    testImplementation(project.project(":common").sourceSets.test.get().output)

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":common", "transformProductionFabric")) { isTransitive = false }
}

modResources {
    properties.put("release_version", modVersion)
    filesMatching.add("fabric.mod.json")
}

tasks {
    base.archivesName.set(base.archivesName.get() + "-fabric")

    processResources {
        from(project(":common").sourceSets.main.get().resources) {
            include("assets/${project.properties["mod_id"]}/**")
        }
        dependsOn(processTestResources)
    }

    processTestResources {
        from(project(":common").sourceSets.test.get().resources) {
            include("assets/${project.properties["mod_id"]}_test/**")
        }
    }

    classes {
        finalizedBy(testClasses)
    }

    shadowJar {
        configurations =
            listOf(
                project.configurations.getByName("shadowCommon"),
                project.configurations.getByName("shadow")
            )
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        injectAccessWidener.set(true)
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
    }

    jar.get().archiveClassifier.set("dev")

    sourcesJar {
        val commonSources = project(":common").tasks.sourcesJar
        dependsOn(commonSources)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(commonSources.get().archiveFile.map { zipTree(it) })
    }
}