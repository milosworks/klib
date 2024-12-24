plugins {
	alias(libs.plugins.archie)
	alias(libs.plugins.architectury.kotlin)
}

dependencies {
	compileOnly(projects.common) { isTransitive = false }

	implementation(projects.neoforge)
}

modResources {
	filesMatching.add("META-INF/neoforge.mods.toml")
}