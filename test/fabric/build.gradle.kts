plugins {
	alias(libs.plugins.archie)
}

modResources {
	filesMatching.add("fabric.mod.json")
}

dependencies {
	if (System.getProperty("idea.sync.active", false.toString()).toBoolean()) {
		compileOnly(projects.common)
	}

	include(implementation(projects.fabric)!!)
}