plugins {
	alias(libs.plugins.archie)
}

modResources {
	filesMatching.add("META-INF/neoforge.mods.toml")
}

dependencies {
	implementation(projects.neoforge)
}