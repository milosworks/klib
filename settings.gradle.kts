enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "klib"

pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/")
		maven("https://maven.architectury.dev/")
		maven("https://maven.minecraftforge.net/")
		maven("https://maven.neoforged.net/releases/")

		maven("https://maven.firstdarkdev.xyz/releases")

		maven("https://maven.milosworks.xyz/releases")

		gradlePluginPortal()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include("common", "fabric", "neoforge")

includeModule("test")

fun includeModule(name: String) {
	includeModule(name, "common")
	includeModule(name, "fabric")
	includeModule(name, "neoforge")
}

fun includeModule(name: String, platform: String) {
	include("$name/$platform")
	project(":$name/$platform").name = "${rootProject.name}-$name-$platform"
}

