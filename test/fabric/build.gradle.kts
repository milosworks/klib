dependencies {
	if (System.getProperty("idea.sync.active", false.toString()).toBoolean()) {
		compileOnly(projects.common)
	}

	include(implementation(projects.fabric)!!)
}