import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.dependencies.DefaultMinimalDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultMutableVersionConstraint

plugins {
	`kotlin-dsl`
	`kotlin-dsl-precompiled-script-plugins`
}

repositories {
	mavenCentral()
	gradlePluginPortal()

//	maven("https://maven.neoforged.net/releases")
//	maven("https://maven.fabricmc.net/")
//	maven("https://maven.architectury.dev/")
//	maven("https://maven.minecraftforge.net/")
}

dependencies {
	implementation(libs.plugins.kotlin.jvm.toLibrary())
//	implementation(libs.plugins.architectury.loom.toLibrary())
//	implementation(libs.plugins.shadow.toLibrary())
}

sourceSets

fun ProviderConvertible<PluginDependency>.toLibrary() = asProvider().toLibrary()

fun Provider<PluginDependency>.toLibrary() = get().toLibrary()

fun PluginDependency.toLibrary() = DefaultMinimalDependency(
	DefaultModuleIdentifier.newId(pluginId, "$pluginId.gradle.plugin"),
	DefaultMutableVersionConstraint(version),
)