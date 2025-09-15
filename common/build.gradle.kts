plugins {
    id("common")
    alias(libs.plugins.moddev)
}

@Suppress("UnstableApiUsage")
neoForge {
    neoFormVersion = libs.versions.neoform.get()

    val at =
        isolated.projectDirectory.file("src/main/resources/META-INF/accesstransformer.cfg").asFile
    if (at.exists() && at.isFile) accessTransformers.from(at.absolutePath)

    parchment {
        minecraftVersion = libs.versions.parchment.mc.get()
        mappingsVersion = libs.versions.parchment.asProvider().get()
    }
}

dependencies {
    compileOnly(libs.mixin)
    // Both Fabric and NeoForge bundle MixinExtras, so we compile against it without including it in our final artifact.
    compileOnly(libs.mixin.extras)
    annotationProcessor(libs.mixin.extras)

    compileOnly(libs.kotlinx.serialization)
    compileOnlyApi(libs.kotlinx.serialization.json)

    api(libs.kotlinx.serialization.cbor) { isTransitive = false }
}

configurations {
    register("commonJava") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("commonKotlin") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
    register("commonResources") {
        isCanBeResolved = false
        isCanBeConsumed = true
    }
}

artifacts {
    add("commonJava", sourceSets.main.get().java.sourceDirectories.singleFile)
    add(
        "commonKotlin",
        sourceSets.main.get().kotlin.sourceDirectories.filter { !it.name.endsWith("java") }.singleFile
    )
    add("commonResources", sourceSets.main.get().resources.sourceDirectories.singleFile)
}