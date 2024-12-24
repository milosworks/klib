import net.kernelpanicsoft.archie.plugin.bundleRuntimeLibrary

plugins {
	alias(libs.plugins.shadow)
	alias(libs.plugins.archie)
	alias(libs.plugins.architectury.kotlin)
}

@Suppress("UnstableApiUsage")
configurations {
	create("common")
	create("shadowCommon")
	compileClasspath.get().extendsFrom(configurations["common"])
	runtimeClasspath.get().extendsFrom(configurations["common"])
}

sourceSets {
	main {
//		resources {
//		}
//		kotlin {
//			srcDir("src/main/gametest")
//		}
//		java {
//			srcDir("src/main/mixin")
//		}
	}
}

dependencies {
	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.nbt)
	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.toml)
	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.json5)
	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.cbor)

	"common"(project(":common", "namedElements")) { isTransitive = false }
	"shadowCommon"(project(":common", "transformProductionFabric")) { isTransitive = false }
}

modResources {
	filesMatching.add("fabric.mod.json")
}

tasks {
	base.archivesName.set(base.archivesName.get() + "-fabric")

	processResources {
		from(project(":common").sourceSets.main.get().resources) {
			include("assets/${project.properties["mod_id"]}/*.png")
		}
	}

	shadowJar {
		configurations =
			listOf(project.configurations.getByName("shadowCommon"), project.configurations.getByName("shadow"))
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