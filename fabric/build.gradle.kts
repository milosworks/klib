import net.kernelpanicsoft.archie.plugin.bundleRuntimeLibrary

plugins {
	alias(libs.plugins.archie)
	alias(libs.plugins.architectury.kotlin)
}

dependencies {
	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.nbt)
	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.toml)
	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.json5)
	bundleRuntimeLibrary(rootProject.libs.kotlinx.serialization.cbor)
}

modResources {
	filesMatching.add("fabric.mod.json")
}

tasks {
	base.archivesName.set(base.archivesName.get() + "-fabric")
}