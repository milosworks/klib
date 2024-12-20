import net.fabricmc.loom.api.LoomGradleExtensionAPI
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
//	alias(libs.plugins.kotlin.compose)
//	alias(libs.plugins.kotlin.compose.plugin) apply false

	alias(libs.plugins.modfusioner)

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
	apply(plugin = "dev.architectury.loom")

	val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom")

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

		if (project.path != ":common") {
			mods {
				maybeCreate("main").apply {
					sourceSet(project.sourceSets.main.get())
					sourceSet(project(":common").sourceSets.main.get())
				}
			}
		}
	}

	repositories {
		mavenCentral()
		mavenLocal()

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
		"mappings"(loom.layered {
			officialMojangMappings()
			parchment(rootProject.libs.parchment)
		})

		compileOnly("org.jetbrains:annotations:24.1.0")
	}
}

val modId = "mod_id".prop
val modVersion = "TAG".env ?: "mod_version".prop!!

allprojects {
	apply(plugin = "java")
	apply(plugin = "idea")
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
//	apply(plugin = "org.jetbrains.compose")
//	apply(plugin = "org.jetbrains.kotlin.plugin.compose")
	apply(plugin = "architectury-plugin")
	apply(plugin = "com.withertech.architectury.kotlin.plugin")
	apply(plugin = "maven-publish")

	version = modVersion
	group = "mod_group".prop!!
	base.archivesName = modId

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

	java {
		toolchain {
			languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_21.toString()))
		}

		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21

		withSourcesJar()
	}
}

//sourceSets {
//	create("testmod") {
//		runtimeClasspath += main.get().runtimeClasspath
//		compileClasspath += main.get().compileClasspath
//	}
//}

//loom {
//	silentMojangMappingsLicense()
//
//	@Suppress("UnstableApiUsage")
//	runs {
//		create("testmodClient") {
//			client()
////			vmArg("-Dmixin.debug.export=true")
//			ideConfigGenerated(project.rootProject == project)
//			name = "TestMod Client"
//			mods {
//				create("testmod") {
//					sourceSet(sourceSets["testmod"])
//				}
//
//				create("klib") {
//					sourceSet(sourceSets.main.get())
//				}
//			}
//			source(sourceSets["testmod"])
//		}
//
//		create("testmodServer") {
//			server()
////			vmArg("-Dmixin.debug.export=true")
//			ideConfigGenerated(project.rootProject == project)
//			name = "TestMod Server"
//			mods {
//				create("testmod") {
//					sourceSet(sourceSets["testmod"])
//				}
//
//				create("klib") {
//					sourceSet(sourceSets.main.get())
//				}
//			}
//			source(sourceSets["testmod"])
//		}
//	}
//}
//
//@Suppress("UnstableApiUsage")
//dependencies {
//	minecraft(libs.minecraft)
//	mappings(loom.layered {
//		officialMojangMappings()
//		parchment(libs.parchment)
//	})
//	neoForge(libs.neoforge)
//
//	implementation(libs.kotlin.neoforge) {
//		exclude("net.neoforged.fancymodloader", "loader")
//	}
//
//	compileOnly(libs.kotlin.stdlib)
//	compileOnly(libs.kotlinx.serialization)
//	kotlinForgeRuntimeLibrary(libs.kotlinx.serialization.cbor)
//	kotlinForgeRuntimeLibrary(libs.kotlinx.serialization.nbt)
//
//	"testmodImplementation"(sourceSets.main.get().output)
//}

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
