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
    neoForge()
}

@Suppress("UnstableApiUsage")
configurations {
    create("common")
    create("testCommon")
    create("shadowCommon")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    testCompileClasspath.get().extendsFrom(compileClasspath.get(), configurations["testCommon"])
    testRuntimeClasspath.get().extendsFrom(runtimeClasspath.get(), configurations["testCommon"])
//    getByName("developmentNeoForge").extendsFrom(configurations["common"])
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    mods {
        maybeCreate("main").apply {
            sourceSet(project.sourceSets.main.get())
        }
        create("test").apply {
            sourceSet(project.sourceSets.test.get())
        }
    }

    runs {
        getByName("client") {
            source(sourceSets.main.get())
            source(sourceSets.test.get())

//            System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug")

            programArgs("--username", "Vyrek_", "--quickPlaySingleplayer", "Test")
        }
    }
}

//sourceSets {
//	main {
//		resources {
//			srcDir("src/main/generated")
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
    compileOnly(libs.kotlin.stdlib)

    neoForge(libs.neoforge)
    modApi(libs.architectury.neoforge)
    implementation(libs.kotlin.neoforge) {
        exclude(group = "net.neoforged.fancymodloader", module = "loader")
    }

    bundleRuntimeLibrary(libs.kotlinx.serialization.nbt)
    bundleRuntimeLibrary(libs.kotlinx.serialization.toml)
    bundleRuntimeLibrary(libs.kotlinx.serialization.json5)
    bundleRuntimeLibrary(libs.kotlinx.serialization.cbor)
    bundleRuntimeLibrary(compose.runtime)

    testImplementation(project.project(":common").sourceSets.test.get().output)

    "common"(project(":common", "namedElements")) { isTransitive = false }
    "testCommon"(project(":common", "testNamedElements")) { isTransitive = false }
    "shadowCommon"(project(":common", "transformProductionNeoForge")) { isTransitive = false }
}

modResources {
    properties.put("release_version", modVersion)
    filesMatching.add("META-INF/neoforge.mods.toml")
}

tasks {
    base.archivesName.set(base.archivesName.get() + "-neoforge")

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
        exclude("fabric.mod.json")
        configurations =
            listOf(
                project.configurations.getByName("shadowCommon"),
                project.configurations.getByName("shadow")
            )
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
//		atAccessWideners.set(setOf(loom.accessWidenerPath.get().asFile.path))
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