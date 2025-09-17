architectury {
    common("fabric", "neoforge")
}

loom {
    accessWidenerPath = file("src/main/resources/${project.properties["mod_id"]}.accesswidener")
}

//sourceSets {
//	main {
//		kotlin {
//			srcDir("src/main/gametest")
//			srcDir("src/main/datagen")
//		}
//		java {
//			srcDir("src/main/mixin")
//		}
//	}
//}

dependencies {
    compileOnly(libs.kotlinx.serialization)
    compileOnlyApi(libs.kotlinx.serialization.json)
    compileOnly(kotlin("reflect"))

    api(libs.kotlinx.serialization.nbt) { isTransitive = false }
    api(libs.kotlinx.serialization.toml) { isTransitive = false }
    api(libs.kotlinx.serialization.json5) { isTransitive = false }
    api(libs.kotlinx.serialization.cbor) { isTransitive = false }

    api(compose.runtime)

    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation(libs.fabric.loader)
    modApi(libs.architectury.common)
}

tasks {
    base.archivesName.set(base.archivesName.get() + "-common")
}

val testNamedElements by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    extendsFrom(configurations.testApi.get())
}

val testJar by tasks.registering(Jar::class) {
    from(sourceSets.test.get().output)
    archiveClassifier.set("test")
}

artifacts {
    add("testNamedElements", testJar) {
        builtBy(testJar)
    }
}