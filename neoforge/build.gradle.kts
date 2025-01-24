import net.kernelpanicsoft.archie.plugin.bundleRuntimeLibrary

plugins {
	alias(libs.plugins.shadow)
	alias(libs.plugins.archie)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.compose.plugin)

	`dokka-convention`
}

architectury {
	platformSetupLoomIde()
	neoForge()
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
	compileOnly(libs.kotlin.stdlib)
	testCompileOnly(libs.kotlin.stdlib)

	neoForge(libs.neoforge)
	modApi(libs.architectury.neoforge)
	implementation(libs.kotlin.neoforge) {
		exclude(group = "net.neoforged.fancymodloader", module = "loader")
	}

	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.nbt)
	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.toml)
	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.json5)
	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.cbor)

	bundleRuntimeLibrary(compose.runtime)

	testImplementation(project.project(":common").sourceSets.test.get().output)

	"common"(project(":common", "namedElements")) { isTransitive = false }
	"shadowCommon"(project(":common", "transformProductionNeoForge")) { isTransitive = false }
}

modResources {
	filesMatching.add("META-INF/neoforge.mods.toml")
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
		exclude("fabric.mod.json")
		configurations =
			listOf(project.configurations.getByName("shadowCommon"), project.configurations.getByName("shadow"))
		archiveClassifier.set("dev-shadow")
	}

	remapJar {
		inputFile.set(shadowJar.get().archiveFile)
//		atAccessWideners.set(setOf(loom.accessWidenerPath.get().asFile.name))
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

base.archivesName.set(base.archivesName.get() + "-neoforge")

dokka {
	moduleName.set("NeoForge")

	dokkaSourceSets.configureEach {
		includes.from("ModuleNeoForge.md")
	}
}