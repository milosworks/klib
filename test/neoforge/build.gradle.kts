dependencies {
	if (System.getProperty("idea.sync.active", false.toString()).toBoolean()) {
		compileOnly(projects.common)
	}

	implementation(projects.neoforge)
}