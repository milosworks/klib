pluginManagement {
	repositories {
		maven("https://maven.neoforged.net/releases")
		maven("https://maven.parchmentmc.org")
		gradlePluginPortal()
	}
}

plugins {
	id ("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "testmod"