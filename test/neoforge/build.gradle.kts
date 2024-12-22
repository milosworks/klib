plugins {
	alias(libs.plugins.archie)
	alias(libs.plugins.architectury.kotlin)
}

dependencies {
	if (System.getProperty("idea.sync.active", false.toString()).toBoolean()) {
		compileOnly(projects.common)
	}

	implementation(projects.neoforge)
}

modResources {
	filesMatching.add("META-INF/neoforge.mods.toml")
}