import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    `java-library`
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

dokka {
    dokkaSourceSets.configureEach {
        sourceLink {
            localDirectory.set(project.projectDir.resolve("src/main"))
            remoteUrl("https://example.com/src")
        }

        perPackageOption {
            documentedVisibilities(
                VisibilityModifier.Public,
                VisibilityModifier.Package,
                VisibilityModifier.Protected
            )
        }
    }
}