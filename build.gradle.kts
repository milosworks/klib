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

	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)

	alias(libs.plugins.architectury)
	alias(libs.plugins.architectury.kotlin)
	alias(libs.plugins.architectury.loom) apply false

//	alias(libs.plugins.kotlin.compose)
//	alias(libs.plugins.kotlin.compose.plugin) apply false

	alias(libs.plugins.modfusioner)
	alias(libs.plugins.archie) apply false

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

subprojects {
	apply(plugin = "java")
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

	apply(plugin = "dev.architectury.loom")
	apply(plugin = "architectury-plugin")
	apply(plugin = "com.withertech.architectury.kotlin.plugin")

	apply(plugin = "maven-publish")

	//	apply(plugin = "org.jetbrains.compose")
//	apply(plugin = "org.jetbrains.kotlin.plugin.compose")

	val parentName = project.layout.projectDirectory.asFile.parentFile.name
	val modLoader = project.layout.projectDirectory.asFile.name

	val isCommon = modLoader == "common"
	val isNeoForge = modLoader == "neoforge"
	val isFabric = modLoader == "fabric"

	val commonPath = when {
		isCommon -> project.name
		parentName != "test" -> ":common"
		else -> ":${rootProject.name}-$parentName-common"
	}

	base {
		archivesName.set("${project.name}-${rootProject.libs.versions.minecraft.get()}")
	}

	configure<LoomGradleExtensionAPI> {
		silentMojangMappingsLicense()

		runs {
			named("client") {
				name("Test Client")
				source(sourceSets.test.get())
			}
			named("server") {
				name("Test Server")
				source(sourceSets.test.get())
			}
		}
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

	if (!isCommon) {
		configure<ArchitectPluginExtension> {
			platformSetupLoomIde()
		}

		sourceSets {
			val commonSourceSets = project(commonPath).sourceSets
			val commonMain = commonSourceSets.getByName("main")

			getByName("main") {
//				java.srcDirs(commonMain.java.srcDirs)
				kotlin.srcDirs(commonMain.kotlin.srcDirs)
//				resources.srcDirs(commonMain.resources.srcDirs)
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
		compileOnly(rootProject.libs.kotlinx.serialization)
		compileOnly(kotlin("reflect"))

		if (isCommon) {
			"modApi"(rootProject.libs.architectury.common)
		}

		if (!isCommon) {
			compileOnly(project(commonPath, configuration = "namedElements"))
		}

		if (isNeoForge) {
			"neoForge"(rootProject.libs.neoforge)
			compileOnly(rootProject.libs.kotlin.stdlib)
			"modApi"(rootProject.libs.architectury.neoforge)
			implementation(rootProject.libs.kotlin.neoforge) {
				exclude(group = "net.neoforged.fancymodloader", module = "loader")
			}
		}

		if (isFabric) {
			"modImplementation"(rootProject.libs.fabric.loader)
			"modApi"(rootProject.libs.fabric.api)
			"modApi"(rootProject.libs.architectury.fabric)
			"modImplementation"(rootProject.libs.kotlin.fabric)
		}
	}

	tasks.named<RemapJarTask>("remapJar") {
		archiveClassifier.set(null as String?)
	}

	java {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_21.toString()))
		}

		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21

		withSourcesJar()

		if (isCommon) {
			tasks.compileJava {
				options.compilerArgs.add("-AgenerateExpectStubs")
			}
		}
	}
}

allprojects {
//	apply(plugin = "com.withertech.architectury.kotlin.plugin")

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
	}

	architectury {
		compileOnly()
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
