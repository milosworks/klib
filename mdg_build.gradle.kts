import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import utils.kotlinForgeRuntimeLibrary

plugins {
	java
	idea
	id("maven-publish")

	alias(libs.plugins.mdg)
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)

	id("utils.mod-resources")
	id("utils.kotlin-runtime-library")
}

val String.prop: String
	get() = project.properties[this] as String

val String.env: String?
	get() = System.getenv(this)

val modId = "mod_id".prop
val modVersion = "TAG".env ?: "mod_version".prop

base.archivesName = modId

repositories {
	maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
	maven("https://thedarkcolour.github.io/KotlinForForge/") {
		name = "Kotlin for Forge"
		content {
			includeGroup("thedarkcolour")
		}
	}
}

dependencies {
	implementation(libs.kotlin.neoforge)

	compileOnly(libs.kotlin.stdlib)
	compileOnly(libs.kotlinx.serialization)
	kotlinForgeRuntimeLibrary(libs.kotlinx.serialization.cbor)
//	api("org.jetbrains.kotlinx", "kotlinx-serialization-core", "1.7.3")
//	api("org.jetbrains.kotlinx", "kotlinx-serialization-cbor-jvm", "1.7.3")
}

neoForge {
	version = libs.versions.neoforge.asProvider()
	validateAccessTransformers = true

	parchment {
		mappingsVersion = libs.versions.parchment.asProvider()
		minecraftVersion = libs.versions.parchment.mc
	}

	mods {
		create(modId) {
			sourceSet(sourceSets["main"])
		}
	}

	runs {
		configureEach {
			systemProperty("forge.logging.markers", "REGISTRIES")
			systemProperty("neoforge.enabledGameTestNamespaces", modId)
			logLevel = org.slf4j.event.Level.DEBUG
		}

		create("client") {
			client()
			programArguments.addAll("--username", "Vyrek_", "--quickPlaySingleplayer", "test")
		}

		create("server") {
			server()
			programArgument("--nogui")
		}

		create("data") {
			data()
			programArguments.addAll(
				"--mod",
				modId,
				"--all",
				"--output",
				file("src/generated/resources/").absolutePath,
				"--existing",
				file("src/main/resources/").absolutePath
			)
		}
	}
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

sourceSets["main"].resources.srcDir("src/generated/resources")

idea {
	module {
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			groupId = project.properties["mod_group_id"] as String
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