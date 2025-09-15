interface ModResourcesExtension {
    val filesMatching: ListProperty<String>
    val versions: MapProperty<String, String>
    val properties: MapProperty<String, String>
}

val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val extension = extensions.create<ModResourcesExtension>("modResources")

extension.filesMatching.set(
    listOf(
        "pack.mcmeta",
        "*.mixins.json"
    )
)

extension.versions.convention(provider {
    val ret = versionCatalog.versionAliases.associate {
        it.replace(".", "_") to versionCatalog.findVersion(it).get().requiredVersion
    }
    if (project.name === "fabric")
        return@provider ret.mapValues { (_, version) ->
            version
                .replace(",", " ")
                .replace(Regex("""\s+"""), " ")
                .replace(Regex("""\[(\S+)"""), ">=$1")
                .replace(Regex("""(\S+)\]"""), "<=$1")
                .replace(Regex("""\](\S+)"""), ">$1")
                .replace(Regex("""(\S+)\["""), "<$1")
        }

    ret
})

extension.properties.convention(provider {
    project.properties.mapKeys { it.key.replace(".", "_") }.mapValues { it.value.toString() }
})

tasks.withType<ProcessResources>().configureEach {
    exclude(".cache")
    val resourceValues = buildMap {
        put("versions", extension.versions.get())
        putAll(extension.properties.get())
    }
    val escapedResourceValues = resourceValues.mapValues { (_, value) ->
        if (value is String) value.replace("\n", "\\\\n") else value
    }

    inputs.properties(resourceValues)

    filesMatching(extension.filesMatching.get()) {
        expand(if (file.name.endsWith(".json") || file.name.endsWith(".packmc")) escapedResourceValues else resourceValues)
    }
}