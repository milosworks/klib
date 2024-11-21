package utils

import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.jetbrains.kotlin.konan.file.file
import org.jetbrains.kotlin.konan.file.use
import java.nio.file.FileSystems
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.registerTransform

/**
 * `forgeRuntimeLibrary` puts classes on the `MC-BOOTSTRAP` layer, but KotlinForForge puts the Kotlin stdlib on the
 * `PLUGIN` layer. This makes it impossible for libraries loaded via `forgeRuntimeLibrary` to access the Kotlin stdlib.
 * As a workaround, this class implements an artifact transform that adds `FMLModType: GAMELIBRARY` to the jar's
 * `MANIFEST.MF` file. This tells Forge to load this library on the `GAME` layer.
 * Note that this isn't an issue in production since JarJar/included libraries are already put on the `GAME` layer.
 */
abstract class PatchFMLModType: TransformAction<PatchFMLModType.Parameters> {
	interface Parameters: TransformParameters

	@get:PathSensitive(PathSensitivity.NAME_ONLY)
	@get:InputArtifact
	abstract val inputArtifact: Provider<FileSystemLocation>

	override fun transform(outputs: TransformOutputs) {
		val inputFile = inputArtifact.get().asFile
		val inputPath = inputFile.toPath()
		val manifest = FileSystems.newFileSystem(inputPath).use { fs ->
			fs.file("/META-INF/MANIFEST.MF").bufferedReader().readText()
		}

		if (manifest.contains("FMLModType")) {
			inputFile.copyTo(outputs.file(inputFile.name))
			return
		}

		val lf = System.lineSeparator()
		val newManifest = manifest.trimEnd() + lf + "FMLModType: GAMELIBRARY" + lf
		val outputFile = outputs.file(
			inputFile.run {"${nameWithoutExtension}-PatchedFMLModType.${extension}"}.trimEnd('.')
		)

		inputFile.copyTo(outputFile)
		FileSystems.newFileSystem(outputFile.toPath()).use { fs ->
			fs.file("/META-INF/MANIFEST.MF").writeText(newManifest)
		}
	}
}

val artifactType = Attribute.of("artifactType", String::class.java)

dependencies {
	attributesSchema {
		attribute(patchedFMLModType)
	}

	artifactTypes.getByName("jar") {
		attributes.attribute(patchedFMLModType, false)
	}

	registerTransform(PatchFMLModType::class) {
		from.attribute(patchedFMLModType, false).attribute(artifactType, "jar")
		to.attribute(patchedFMLModType, true).attribute(artifactType, "jar")
	}
}