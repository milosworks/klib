enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
	repositories {
		maven("https://maven.fabricmc.net/")
		maven("https://maven.architectury.dev/")
		maven("https://maven.minecraftforge.net/")
		maven("https://maven.neoforged.net/releases/")

		maven("https://maven.firstdarkdev.xyz/releases")

		maven("https://maven.milosworks.xyz/releases")
		maven("https://repo.kernelpanicsoft.net/maven/releases")

		mavenCentral()
		gradlePluginPortal()
	}
}

include(":common", ":fabric", ":neoforge")

rootProject.name = "klib"