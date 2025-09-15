import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("common")
}

val modId: String by project

configurations {
    register("commonJava") {
        isCanBeResolved = true
    }
    register("commonKotlin") {
        isCanBeResolved = true
    }
    register("commonResources") {
        isCanBeResolved = true
    }
}

dependencies {
    compileOnly(project(":common")) {
        capabilities {
            requireCapability("$group:$modId-common:${project.version}")
        }
    }

    "commonJava"(project(":common", "commonJava"))
    "commonKotlin"(project(":common", "commonKotlin"))
    "commonResources"(project(":common", "commonResources"))
}

tasks {
    withType<JavaCompile> {
        dependsOn(configurations.named("commonJava"))
        source(configurations.named("commonJava"))
    }

    withType<KotlinCompile> {
//        dependsOn(configurations.named("commonJava"))
        dependsOn(configurations.named("commonKotlin"))
//        source(configurations.named("commonJava"))
        source(configurations.named("commonKotlin"))
    }

    processResources {
        dependsOn(configurations.named("commonResources"))
        from(configurations.named("commonResources"))
    }

    named<Jar>("sourcesJar") {
        dependsOn(configurations.named("commonJava"))
        from(configurations.named("commonJava"))
        dependsOn(configurations.named("commonKotlin"))
        from(configurations.named("commonKotlin"))
        dependsOn(configurations.named("commonResources"))
        from(configurations.named("commonResources"))
    }
}