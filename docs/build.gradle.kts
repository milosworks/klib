plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    id("dokka-convention")
}

dependencies {
    dokka(project(":common"))
    dokka(project(":fabric"))
    dokka(project(":neoforge"))
}

dokka {
    moduleName.set("Project Name")

    dokkaPublications.html {
        failOnWarning = true

        outputDirectory = rootDir.resolve("docs/dokka/")
    }
}