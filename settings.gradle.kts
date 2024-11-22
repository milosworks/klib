pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/")
		maven("https://maven.architectury.dev/")
		maven("https://maven.minecraftforge.net/")

		maven("https://maven.neoforged.net/releases")
		maven("https://maven.parchmentmc.org")

		gradlePluginPortal()
	}

	includeBuild("plugins")
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "testmod"