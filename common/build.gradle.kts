plugins {
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.compose.plugin)
}

architectury {
	common("fabric", "neoforge")
}

dependencies {
	compileOnly(kotlin("reflect"))
	compileOnly(libs.kotlinx.serialization)
	compileOnly(libs.kotlinx.serialization.json)
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