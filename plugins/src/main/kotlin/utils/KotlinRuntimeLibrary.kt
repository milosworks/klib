package utils

import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.attributes.Attribute
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible

val patchedFMLModType = Attribute.of("patchedFMLModType", Boolean::class.javaObjectType)

fun <T: ModuleDependency> DependencyHandlerScope.kotlinForgeRuntimeLibrary(dependency: T, dependencyConfiguration: T.() -> Unit = {}) {
	"include"(dependency) {
		isTransitive = false
		dependencyConfiguration()
	}
	"implementation"(dependency) {
		isTransitive = false
		dependencyConfiguration()
	}
	"localRuntime"(dependency) {
		isTransitive = false
		dependencyConfiguration()
		attributes {
			attribute(patchedFMLModType, true)
		}
	}
}

fun DependencyHandlerScope.kotlinForgeRuntimeLibrary(dependencyAnnotation: String, dependencyConfiguration: ExternalModuleDependency.() -> Unit = {}) {
	"include"(dependencyAnnotation) {
		isTransitive = false
		dependencyConfiguration()
	}
	"implementation"(dependencyAnnotation) {
		isTransitive = false
		dependencyConfiguration()
	}
	"localRuntime"(dependencyAnnotation) {
		isTransitive = false
		dependencyConfiguration()
		attributes {
			attribute(patchedFMLModType, true)
		}
	}
}

fun <T : Any> DependencyHandlerScope.kotlinForgeRuntimeLibrary(dependency: Provider<T>, dependencyConfiguration: ExternalModuleDependency.() -> Unit = {}) {
	"include"(dependency) {
		isTransitive = false
		dependencyConfiguration()
	}
	"implementation"(dependency) {
		isTransitive = false
		dependencyConfiguration()
	}
	"localRuntime"(dependency) {
		isTransitive = false
		dependencyConfiguration()
		attributes {
			attribute(patchedFMLModType, true)
		}
	}
}

fun <T : Any> DependencyHandlerScope.kotlinForgeRuntimeLibrary(dependency: ProviderConvertible<T>, dependencyConfiguration: ExternalModuleDependency.() -> Unit = {}) {
	"include"(dependency) {
		isTransitive = false
		dependencyConfiguration()
	}
	"implementation"(dependency) {
		dependencyConfiguration()
	}
	"localRuntime"(dependency) {
		isTransitive = false
		dependencyConfiguration()
		attributes {
			attribute(patchedFMLModType, true)
		}
	}
}