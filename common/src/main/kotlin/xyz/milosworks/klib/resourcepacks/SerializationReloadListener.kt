package xyz.milosworks.klib.resourcepacks

import com.mojang.logging.LogUtils
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import net.minecraft.resources.FileToIdConverter
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import org.slf4j.Logger
import java.io.InputStreamReader

/**
 * An abstract reload listener that automatically discovers and deserializes resources
 * from a given directory using Kotlinx Serialization.
 *
 * @param T The data class type to which the resources will be deserialized.
 * @param format The Kotlinx Serialization StringFormat (e.g., Json, Toml).
 * @param serializer The serializer for the data class T.
 * @param directory The resource directory to scan (e.g., "klib_themes").
 * @param fileExtension The file extension to look for, including the dot (e.g., ".json", ".toml").
 */
abstract class SerializationReloadListener<T>(
    private val format: StringFormat,
    private val serializer: KSerializer<T>,
    private val directory: String,
    private val fileExtension: String
) : SimplePreparableReloadListener<Map<ResourceLocation, T>>() {
    companion object {
        private val LOGGER: Logger = LogUtils.getLogger()
    }

    override fun prepare(
        resourceManager: ResourceManager,
        profiler: ProfilerFiller
    ): Map<ResourceLocation, T> {
        val dataMap = mutableMapOf<ResourceLocation, T>()
        val fileToIdConverter = FileToIdConverter(directory, fileExtension)

        for ((fileLocation, resource) in fileToIdConverter.listMatchingResources(resourceManager)) {
            val resourceId = fileToIdConverter.fileToId(fileLocation)
            try {
                InputStreamReader(resource.open()).use { reader ->
                    val decodedData = format.decodeFromString(serializer, reader.readText())
                    dataMap[resourceId] = decodedData
                }
            } catch (e: Exception) {
                LOGGER.error("Couldn't parse data file {} from {}", resourceId, fileLocation, e)
            }
        }
        return dataMap
    }
}