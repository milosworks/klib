import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	java
	idea
	id("maven-publish")

	alias(libs.plugins.architectury.loom)
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.serialization)

//	id("utils.kotlin-runtime-library")
//	id("utils.mod-resources")
}

val modId = project.properties["mod_id"] as String
val modVersion = System.getenv("TAG") ?: project.properties["mod_version"] as String

base.archivesName = modId

configure<LoomGradleExtensionAPI> {
	silentMojangMappingsLicense()
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
	"minecraft"(libs.minecraft)
	"mappings"(loom.layered {
		officialMojangMappings()
		parchment(libs.parchment)
	})

	compileOnly(libs.kotlin.stdlib)
	implementation(libs.kotlin.neoforge)

//	neoForge(libs.neoforge)
}

interface ModResourcesExtension {
//	val filesMatching: ListProperty<String>
	val versions: MapProperty<String, String>
	val properties: MapProperty<String, String>
}

val extension = extensions.create<ModResourcesExtension>("modResources")

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
extension.versions.convention(provider {
	versionCatalog.versionAliases.associate {
		it.replace(".", "_") to versionCatalog.findVersion(it).get().requiredVersion
	}
})
extension.properties.convention(provider {
	project.properties.mapKeys {
		it.key.replace(".", "_")
	}.mapValues { it.value.toString() }
})

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

	withType<ProcessResources> {
		exclude(".cache")

		val resourceValues = buildMap {
			put("versions", extension.versions.get())
			putAll(extension.properties.get())
		}

		inputs.properties(resourceValues)

		filesMatching("META-INF/neoforge.mods.toml") {
			expand(resourceValues)
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

//modResources {
//	filesMatching.add("META-INF/neoforge.mods.toml")
//}

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
