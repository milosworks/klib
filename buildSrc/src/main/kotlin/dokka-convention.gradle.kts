import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
	id("org.jetbrains.dokka")
}

dokka {
	dokkaSourceSets.configureEach {
		documentedVisibilities(
			VisibilityModifier.Package,
			VisibilityModifier.Public,
			VisibilityModifier.Protected
		)

		perPackageOption {
			matchingRegex.set(".*internal.*")
		}

		sourceLink {
			remoteUrl("https://github.com/milosworks/klib/tree/main/")
			remoteLineSuffix = "#L"
			localDirectory = rootDir
		}
	}

	pluginsConfiguration.html {
		footerMessage.set("MilosWorks")
	}
}