import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("dokka-convention")
    `maven-publish`
}

val modId: String by project
val modName: String by project
val modAuthor: String by project

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val minecraftVersion = libs.findVersion("minecraft").get().requiredVersion
val javaVer = libs.findVersion("java").get().requiredVersion

base.archivesName = "$modId-${project.name}"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVer))
    withSourcesJar()
}

kotlin {
    jvmToolchain(javaVer.toInt())
}

repositories {
    mavenCentral()

    maven("https://maven.blamejared.com") { name = "BlameJared" }
    maven("https://thedarkcolour.github.io/KotlinForForge/") { name = "kotlinforforge" }

    exclusiveContent {
        forRepositories(
            maven("https://maven.parchmentmc.org/") { name = "ParchmentMC" },
            maven("https://maven.neoforged.net/releases") { name = "NeoForge" }
        )
        filter {
            includeGroup("org.parchmentmc.data")
        }
    }
}

val publicationVariants = listOf(
    "apiElements",
    "runtimeElements",
    "sourcesElements",
//    "javadocElements"
)

// Configure Gradle's capabilities to allow other subprojects to depend on this common module
// without specifying the loader.
publicationVariants.forEach { variantName ->
    configurations.getByName(variantName) {
        outgoing {
            // "com.example.mod:my-mod-common:1.0.0"
            capability("${project.group}:${base.archivesName.get()}:${project.version}")

            // "com.example.mod:my-mod:1.0.0"
            // This lets other projects request the simple name, and Gradle will automatically
            // select the correct loader-specific artifact (Forge, Fabric, etc.).
            capability("${project.group}:$modId:${project.version}")
        }
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(javaVer.toInt())
    }

    withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVer))

            // Here you can add Kotlin compiler arguments and opt-ins.
            // optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
            // freeCompilerArgs.add("-Xwhen-guards")
        }
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_$modId" }
        }

        manifest {
            attributes(
                mapOf(
                    "Specification-Title" to modName,
                    "Specification-Vendor" to modAuthor,
                    "Specification-Version" to project.version,
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to modAuthor,
                    "Built-On-Minecraft" to minecraftVersion
                )
            )
        }
    }

    named<Jar>("sourcesJar") {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_$modId" }
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = "$modId-${project.name}-${project.version}-${minecraftVersion}"
            from(components["java"])
        }

        configureEach {
            if (this is MavenPublication) {
                listOf(
                    "apiElements",
                    "runtimeElements",
                    "sourcesElements",
                    "javadocElements"
                ).forEach { variantName ->
                    suppressPomMetadataWarningsFor(variantName)
                }
            }
        }
    }

    repositories {
        // Define your repository here, e.g., MavenLocal for testing
        // mavenLocal()
        // Or a remote repository
        // maven {
        //     url = uri("https://your.maven.repo/releases")
        //     credentials {
        //         username = System.getenv("MAVEN_USERNAME")
        //         password = System.getenv("MAVEN_PASSWORD")
        //     }
        // }
    }
}