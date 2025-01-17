import net.kernelpanicsoft.archie.plugin.bundleMod
import net.kernelpanicsoft.archie.plugin.bundleRuntimeLibrary

plugins {
	alias(libs.plugins.shadow)
	alias(libs.plugins.archie)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.compose.plugin)
}

architectury {
	platformSetupLoomIde()
	fabric()
}

@Suppress("UnstableApiUsage")
configurations {
	create("common")
	create("shadowCommon")
	compileClasspath.get().extendsFrom(configurations["common"])
	runtimeClasspath.get().extendsFrom(configurations["common"])
	testCompileClasspath.get().extendsFrom(compileClasspath.get())
	testRuntimeClasspath.get().extendsFrom(runtimeClasspath.get())
}

loom {
	mods {
		maybeCreate("main").apply {
			sourceSet(project.sourceSets.main.get())
			sourceSet(project(":common").sourceSets.main.get())
		}
		create("test") {
			sourceSet(project.sourceSets.test.get())
			sourceSet(project(":common").sourceSets.test.get())
		}
	}

	runs {
		getByName("client") {
			source(sourceSets.test.get())
		}
	}
}

dependencies {
	modImplementation(libs.fabric.loader)
	modApi(libs.fabric.api)
	modApi(libs.architectury.fabric)
	modImplementation(libs.kotlin.fabric)

	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.nbt)
	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.toml)
	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.json5)
	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.cbor)
	bundleRuntimeLibrary(compose.runtime)

	testImplementation(project.project(":common").sourceSets.test.get().output)

	"common"(project(":common", "namedElements")) { isTransitive = false }
	"shadowCommon"(project(":common", "transformProductionFabric")) { isTransitive = false }
}

modResources {
	filesMatching.add("fabric.mod.json")
}

tasks {
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
		configurations = listOf(
			project.configurations.getByName("shadowCommon"),
			project.configurations.getByName("shadow")
		)
		archiveClassifier.set("dev-shadow")
	}

	remapJar {
//		injectAccessWidener.set(true)
		inputFile.set(shadowJar.get().archiveFile)
		dependsOn(shadowJar)
	}

	jar.get().archiveClassifier.set("dev")

	sourcesJar {
		project(":common").tasks.sourcesJar.also {
			dependsOn(it)
			from(it.get().archiveFile.map { zipTree(it) })
		}
		duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	}
}

base.archivesName.set(base.archivesName.get() + "-fabric")