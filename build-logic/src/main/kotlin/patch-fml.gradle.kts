import org.jetbrains.kotlin.konan.file.file
import xyz.milosworks.plugin.patchedFMLModType
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path


abstract class PatchFMLModType : TransformAction<PatchFMLModType.Parameters> {
    interface Parameters : TransformParameters

    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputArtifact
    abstract val inputArtifact: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        val inputFile: File = inputArtifact.get().asFile
        val inputPath: Path = inputFile.toPath()

        val manifest: String = FileSystems.newFileSystem(inputPath).use { fs: FileSystem ->
            fs.file("/META-INF/MANIFEST.MF").bufferedReader().readText()
        }

        if (manifest.contains("FMLModType")) {
            inputFile.copyTo(outputs.file(inputFile.name))
            return
        }

        val lf: String = System.lineSeparator()
        val newManifest: String = manifest.trimEnd() + lf + "FMLModType: GAMELIBRARY" + lf

        val outputFile: File = outputs.file(
            inputFile.run { "${nameWithoutExtension}-PatchedFMLModType.${extension}" }.trimEnd('.')
        )

        inputFile.copyTo(outputFile)
        FileSystems.newFileSystem(outputFile.toPath()).use { fs: FileSystem ->
            fs.file("/META-INF/MANIFEST.MF").writeText(newManifest)
        }
    }
}

val artifactType: Attribute<String> = Attribute.of("artifactType", String::class.java)

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