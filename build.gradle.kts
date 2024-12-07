import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import utils.kotlinForgeRuntimeLibrary

plugins {
	java
	idea
	id("maven-publish")

	alias(libs.plugins.architectury.loom)
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)

	alias(libs.plugins.dokka)

	id("utils.kotlin-runtime-library")
	id("utils.mod-resources")
}

val String.prop: String?
	get() = project.properties[this] as String?

val String.env: String?
	get() = System.getenv(this)

val modId = "mod_id".prop
val modVersion = "TAG".env ?: "mod_version".prop

base.archivesName = modId

sourceSets {
	create("testmod") {
		runtimeClasspath += main.get().runtimeClasspath
		compileClasspath += main.get().compileClasspath
	}
}

loom {
	silentMojangMappingsLicense()

	runs {
		create("testmodClient") {
			client()
			ideConfigGenerated(project.rootProject == project)
			name = "TestMod Client"
			mods {
				create("testmod") {
					sourceSet(sourceSets.getByName("testmod"))
				}

				create("klib") {
					sourceSet(sourceSets.main.get())
				}
			}
			source(sourceSets.getByName("testmod"))
		}
	}
}

repositories {
	mavenCentral()
	mavenLocal()

	maven("https://maven.neoforged.net/releases") { name = "NeoForged" }
	maven("https://thedarkcolour.github.io/KotlinForForge/") {
		name = "Kotlin for Forge"
		content {
			includeGroup("thedarkcolour")
		}
	}
}

@Suppress("UnstableApiUsage")
dependencies {
	minecraft(libs.minecraft)
	mappings(loom.layered {
		officialMojangMappings()
		parchment(libs.parchment)
	})
	neoForge(libs.neoforge)

	implementation(libs.kotlin.neoforge) {
		exclude("net.neoforged.fancymodloader", "loader")
	}

	compileOnly(libs.kotlin.stdlib)
	compileOnly(libs.kotlinx.serialization)
	kotlinForgeRuntimeLibrary(libs.kotlinx.serialization.cbor)
	kotlinForgeRuntimeLibrary(libs.kotlinx.serialization.nbt)

//	"testmodImplementation"(sourceSets.main)
}

tasks {
	withType<JavaCompile> {
		options.encoding = "UTF-8"
		sourceCompatibility = JavaVersion.VERSION_21.toString()
		targetCompatibility = JavaVersion.VERSION_21.toString()
		options.release.set(JavaVersion.VERSION_21.toString().toInt())
	}

	withType<KotlinCompile> {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_21)

			optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
		}
	}

	jar {
		archiveFileName.set("${modId}-${modVersion}.jar")
		from("LICENSE")
	}

	java {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_21.toString()))
		}

		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21

		withSourcesJar()
	}
}

dokka {
	moduleName.set("KLibrary")

	dokkaSourceSets.main {
		documentedVisibilities(
			VisibilityModifier.Package,
			VisibilityModifier.Public,
			VisibilityModifier.Protected
		)

		perPackageOption {
			matchingRegex.set(".*internal.*")
			suppress.set(true)
		}

		sourceLink {
			localDirectory.set(file("src/main/kotlin"))
			remoteUrl("https://github.com/milosworks/klib")
			remoteLineSuffix.set("#L")
		}
	}

	pluginsConfiguration.html {
		footerMessage.set("MilosWorks")
	}

	dokkaPublications.html {
		suppressInheritedMembers = true
		outputDirectory.set(layout.buildDirectory.dir("docs"))
	}
}

idea {
	module {
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			groupId = "mod_group_id".prop
			artifactId = modId
			version = modVersion

			from(components["java"])
		}
	}
	repositories {
		maven {
			url = uri("file://${project.projectDir}/repo")
		}
	}
}
