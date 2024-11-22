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
}

dependencies {
	implementation(libs.plugins.kotlin.jvm.toLibrary())
}

fun ProviderConvertible<PluginDependency>.toLibrary() = asProvider().toLibrary()

fun Provider<PluginDependency>.toLibrary() = get().toLibrary()

fun PluginDependency.toLibrary() = DefaultMinimalDependency(
	DefaultModuleIdentifier.newId(pluginId, "$pluginId.gradle.plugin"),
	DefaultMutableVersionConstraint(version),
)