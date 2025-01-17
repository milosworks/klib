import dev.architectury.plugin.ArchitectPluginExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
	java
	idea

	alias(libs.plugins.architectury)
	alias(libs.plugins.architectury.kotlin) apply false
	alias(libs.plugins.architectury.loom) apply false

	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.kotlin.compose) apply false
	alias(libs.plugins.kotlin.compose.plugin) apply false

	alias(libs.plugins.modfusioner)
//	alias(libs.plugins.modpublisher)

	alias(libs.plugins.dokka)
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

		maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
		maven("https://maven.parchmentmc.org")
		maven("https://maven.fabricmc.net/")
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
	apply(plugin = "idea")

	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
	apply(plugin = "org.jetbrains.compose")
	apply(plugin = "org.jetbrains.kotlin.plugin.compose")

	apply(plugin = "architectury-plugin")
	apply(plugin = "com.withertech.architectury.kotlin.plugin")

//	apply(plugin = "maven-publish")

	version = modVersion
	group = "mod_group".prop!!
	base.archivesName = "mod_id".prop!!

	tasks {
		withType<JavaCompile> {
			options.encoding = "UTF-8"
			options.release.set(JavaVersion.VERSION_21.toString().toInt())
		}

		withType<KotlinCompile> {
			compilerOptions {
				jvmTarget.set(JvmTarget.JVM_21)

				optIn.add("kotlinx.serialization.ExperimentalSerializationApi")
			}
		}
	}

	java.withSourcesJar()

	architectury {
		compileOnly()
	}

	idea {
		module {
			isDownloadSources = true
			isDownloadJavadoc = true
		}
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
		}

		sourceLink {
//			localDirectory.set(file("src/main/kotlin"))
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

fusioner {
	packageGroup = project.group.toString()
	mergedJarName = "${project.base.archivesName.get()}-merged-${libs.versions.minecraft.get()}"
	jarVersion = project.version.toString()
	outputDirectory = "build/artifacts"

	fabric {
		inputTaskName = "remapJar"
	}

	neoforge {
		inputTaskName = "remapJar"
	}
}

tasks {
	build {
		finalizedBy(fusejars)
	}
	assemble {
		finalizedBy(fusejars)
	}
}

idea {
	module {
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}

//publishing {
//	publications {
//		create<MavenPublication>("mavenJava") {
//			groupId = "mod_group_id".prop
//			artifactId = modId
//			version = modVersion
//
//			from(components["java"])
//		}
//	}
//	repositories {
//		maven {
//			url = uri("file://${project.projectDir}/repo")
//		}
//
//		maven {
//			name = "milosworks"
//			url = uri("https://maven.milosworks.xyz/snapshots")
//			credentials(PasswordCredentials::class)
//			authentication {
//				create<BasicAuthentication>("basic")
//			}
//		}
//	}
//}
