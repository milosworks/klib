plugins {
	id("org.jetbrains.dokka")
}

dokka {
//	moduleName.set("KLibrary")

//	dokkaSourceSets.configureEach {
//		documentedVisibilities(
//			VisibilityModifier.Package,
//			VisibilityModifier.Public,
//			VisibilityModifier.Protected
//		)
//
//		perPackageOption {
//			matchingRegex.set(".*internal.*")
//		}
//
//		sourceLink {
//			localDirectory.set(rootDir)
//			remoteUrl("https://github.com/milosworks/klib")
//			remoteLineSuffix.set("#L")
//		}
//	}

	pluginsConfiguration.html {
		footerMessage.set("MilosWorks")
	}

//	dokkaPublications.html {
//		suppressInheritedMembers = true
//		outputDirectory.set(rootProject.layout.buildDirectory.dir("dokkaDocs"))
//	}
}