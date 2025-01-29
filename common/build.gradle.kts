plugins {
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.compose.plugin)

	`dokka-convention`
}

loom {
	accessWidenerPath = file("src/main/resources/${project.properties["mod_id"]}.accesswidener")
}

architectury {
	common("fabric", "neoforge")
}

dependencies {
	compileOnly(kotlin("reflect"))
	compileOnlyApi(libs.kotlinx.serialization)
	compileOnly(libs.kotlinx.serialization.json)

	testCompileOnly(kotlin("reflect"))
	testCompileOnly(libs.kotlinx.serialization)
	testCompileOnly(libs.kotlinx.serialization.json)

	api(libs.kotlinx.serialization.nbt) { isTransitive = false }
	api(libs.kotlinx.serialization.toml) { isTransitive = false }
	api(libs.kotlinx.serialization.json5) { isTransitive = false }
	api(libs.kotlinx.serialization.cbor) { isTransitive = false }

	api(compose.runtime)

	modApi(libs.architectury.common)
}

tasks {
	base.archivesName.set(base.archivesName.get() + "-common")
}

dokka {
	moduleName.set("Common")

	dokkaSourceSets.configureEach {
		includes.from("ModuleCommon.md")
	}
}