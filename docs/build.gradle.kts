plugins {
	kotlin("jvm") apply false
	`dokka-convention`
}

dependencies {
	dokka(projects.common)
	dokka(projects.fabric)
	dokka(projects.neoforge)
}

dokka {
	moduleName.set("KLibrary")

	dokkaPublications.html {
		includes.from("MainModule.md")
	}
}