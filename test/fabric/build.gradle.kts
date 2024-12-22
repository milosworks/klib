plugins {
	alias(libs.plugins.archie)
	alias(libs.plugins.architectury.kotlin)
}

dependencies {
	if (System.getProperty("idea.sync.active", false.toString()).toBoolean()) {
		compileOnly(projects.common)
	}

	include(implementation(projects.fabric)!!)
}

modResources {
	filesMatching.add("fabric.mod.json")
}