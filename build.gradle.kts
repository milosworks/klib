import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
	java

	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.kotlin.compose)

	alias(libs.plugins.architectury)
	alias(libs.plugins.architectury.kotlin)
	alias(libs.plugins.architectury.loom) apply false

	alias(libs.plugins.compose)
}

architectury.minecraft = libs.versions.minecraft.get()

val localProperties = kotlin.runCatching { loadProperties("$rootDir/local.properties") }.getOrNull()

val String.prop: String?
	get() = project.properties[this] as String?

val String.env: String?
	get() = System.getenv(this)

val String.localOrEnv: String?
	get() = localProperties?.get(this)?.toString() ?: System.getenv(this.uppercase())

val modVersion = ("TAG".env ?: "mod_version".prop)!!

subprojects {
	apply(plugin = "dev.architectury.loom")

	configure<LoomGradleExtensionAPI> {
		silentMojangMappingsLicense()
	}

	repositories {
		mavenCentral()
		mavenLocal()
		google()

		maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
		maven("https://maven.parchmentmc.org")
		maven("https://maven.fabricmc.net/")
		maven("https://maven.neoforged.net/releases/")
		maven("https://thedarkcolour.github.io/KotlinForForge/")
	}

	@Suppress("UnstableApiUsage")
	dependencies {
		"minecraft"(rootProject.libs.minecraft)
		"mappings"(project.the<LoomGradleExtensionAPI>().layered {
			officialMojangMappings()
			parchment(rootProject.libs.parchment)
		})

		compileOnly("org.jetbrains:annotations:24.1.0")
	}
}

allprojects {
	apply(plugin = "java")

	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
	apply(plugin = "org.jetbrains.kotlin.plugin.compose")
	apply(plugin = "org.jetbrains.compose")

	apply(plugin = "architectury-plugin")

//	apply(plugin = "maven-publish")

	version = modVersion
	group = "mod_group".prop!!
	base.archivesName = "mod_id".prop!!

	tasks {
		withType<JavaCompile> {
			options.encoding = "UTF-8"
			options.release.set(21)
		}

		withType<KotlinCompile> {
			compilerOptions {
				jvmTarget.set(JvmTarget.JVM_21)

				optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
				freeCompilerArgs.add("-Xwhen-guards")
			}
		}
	}

	architectury {
		compileOnly()
	}

	java.withSourcesJar()
}

dependencies {
	// dokka projects
}
