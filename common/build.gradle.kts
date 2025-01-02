plugins {
	alias(libs.plugins.architectury.kotlin)
}

dependencies {
	compileOnly(libs.kotlinx.serialization.json)
	api(libs.kotlinx.serialization.nbt) { isTransitive = false }
	api(libs.kotlinx.serialization.toml) { isTransitive = false }
	api(libs.kotlinx.serialization.json5) { isTransitive = false }
	api(libs.kotlinx.serialization.cbor) { isTransitive = false }
}

//tasks {
//	base.archivesName.set(base.archivesName.get() + "-common")
//}