package xyz.milosworks.plugin

import gradle.kotlin.dsl.accessors._2cac6106f27f338e640af273e85b4d2a.implementation
import gradle.kotlin.dsl.accessors._2cac6106f27f338e640af273e85b4d2a.runtimeClasspath
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.attributes.Attribute
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.DependencyHandlerScope

val patchedFMLModType = Attribute.of("patchedFMLModType", Boolean::class.javaObjectType)

fun <T : Any> DependencyHandlerScope.bundleRuntimeLibrary(
    dependency: Provider<T>,
    dependencyConfiguration: ExternalModuleDependency.() -> Unit = {}
) {
    "jarJar"(dependency) {
        isTransitive = false
        dependencyConfiguration()
    }
    implementation(dependency) {
        dependencyConfiguration()
    }
    runtimeClasspath(dependency) {
        isTransitive = false
        dependencyConfiguration()
        attributes {
            attribute(patchedFMLModType, true)
        }
    }
}