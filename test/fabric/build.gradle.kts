plugins {
	alias(libs.plugins.archie)
	alias(libs.plugins.architectury.kotlin)
}

dependencies {
	compileOnly(projects.common)

	include(implementation(projects.fabric)!!)
}

modResources {
	filesMatching.add("fabric.mod.json")
}