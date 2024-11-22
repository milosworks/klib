package utils

interface ModResourcesExtension {
	val versions: MapProperty<String, String>
	val properties: MapProperty<String, String>
}

val extension = extensions.create<ModResourcesExtension>("modResources")

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
extension.versions.convention(provider {
	versionCatalog.versionAliases.associate {
		it.replace(".", "_") to versionCatalog.findVersion(it).get().requiredVersion
	}
})
extension.properties.convention(provider {
	project.properties.mapKeys {
		it.key.replace(".", "_")
	}.mapValues { it.value.toString() }
})

tasks.withType<ProcessResources>().configureEach {
	exclude(".cache")

	val resourceValues = buildMap {
		put("versions", extension.versions.get())
		putAll(extension.properties.get())
	}

	inputs.properties(resourceValues)

	filesMatching("META-INF/neoforge.mods.toml") {
		expand(resourceValues)
	}
}