architectury {
	common("fabric", "neoforge")
	platformSetupLoomIde()
}

dependencies {
	compileOnly(libs.kotlinx.serialization)
	compileOnly(kotlin("reflect"))
	compileOnly(libs.kotlinx.serialization.json)
	api(libs.kotlinx.serialization.nbt) { isTransitive = false }
	api(libs.kotlinx.serialization.toml) { isTransitive = false }
	api(libs.kotlinx.serialization.json5) { isTransitive = false }
	api(libs.kotlinx.serialization.cbor) { isTransitive = false }
//	modImplementation(libs.fabric.loader)

	modApi(libs.architectury.common)
}

tasks {
	base.archivesName.set(base.archivesName.get() + "-common")
}