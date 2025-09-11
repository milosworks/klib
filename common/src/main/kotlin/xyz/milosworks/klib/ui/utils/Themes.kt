package xyz.milosworks.klib.ui.utils

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import xyz.milosworks.klib.KLib
import xyz.milosworks.klib.serialization.serializers.SResourceLocation
import xyz.milosworks.klib.ui.composables.theme.TextureStates
import xyz.milosworks.klib.ui.layout.primitive.Size

@Serializable
sealed interface ThemeState {
    val texture: SResourceLocation

    @SerialName("texture_size")
    val textureSize: Size
    val u: Int
    val v: Int
}

@Serializable
data class NinePatchThemeState(
    override val texture: SResourceLocation,
    @SerialName("texture_size")
    override val textureSize: Size,
    override val u: Int = 0,
    override val v: Int = 0,
    val repeat: Boolean = false,
    @SerialName("corners_size")
    val cornersSize: Size,
    @SerialName("center_size")
    val centerSize: Size,
) : ThemeState

@Serializable
data class SimpleThemeState(
    override val texture: SResourceLocation,
    @SerialName("texture_size")
    override val textureSize: Size,
    override val u: Int = 0,
    override val v: Int = 0,
    val width: Int,
    val height: Int,
    @SerialName("u_width")
    val uWidth: Int,
    @SerialName("v_height")
    val vHeight: Int,
) : ThemeState

@Serializable
data class StatefulTheme(val states: Map<String, ThemeState>)

@Serializable
data class ComposableTheme(
    @SerialName("ninepatch")
    val isNinepatch: Boolean,
    val states: Map<String, ThemeState>,
    val variants: Map<String, StatefulTheme> = emptyMap(),
) {
    companion object {
        operator fun get(loc: ResourceLocation): ComposableTheme =
            ThemeResourceListener.COMPOSABLES[loc]
                ?: throw IllegalStateException("No theme found for composable: $loc")
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun getState(stateName: String, variantName: String): ThemeState =
        variants[variantName]?.states[stateName] ?: states[stateName]
        ?: states[TextureStates.DEFAULT]!!

    @Suppress("NOTHING_TO_INLINE")
    inline fun hasState(stateName: String, variantName: String?): Boolean =
        (variantName?.let { variants[it] }?.states[stateName] ?: states[stateName]) != null
}

// REMEMBER: SimpleJsonResourceReloadListener expects a type in 1.21.4 in this case being NinePatchTexture

class ThemeResourceListener : SimpleJsonResourceReloadListener(Gson(), "klib_themes"),
    PreparableReloadListener {
    companion object {
        internal val COMPOSABLES = mutableMapOf<ResourceLocation, ComposableTheme>()
    }

    override fun apply(
        objs: Map<ResourceLocation?, JsonElement?>,
        resourceManager: ResourceManager,
        profiler: ProfilerFiller
    ) {
        for ((location, el) in objs) {
            if (location == null || el !is JsonObject) continue

            try {
                val isNinepatch = el["ninepatch"]?.asBoolean != false
                val statesObj = el["states"].asJsonObject
                if (statesObj !is JsonObject) throw IllegalStateException("Theme must have a valid states object: $location")

                val defaultObj = statesObj["default"].asJsonObject
                if (defaultObj !is JsonObject) throw IllegalStateException("Theme must have a valid \"default\" state object: $location")

                val defaultState =
                    if (isNinepatch) parseNinePatchState(location, "default", defaultObj)
                    else parseSimpleState(location, "default", defaultObj)

                val states = mutableMapOf<String, ThemeState>()
                for ((stateName, stateObj) in statesObj.entrySet()) {
                    if (stateObj !is JsonObject) throw IllegalStateException("State \"$stateName\" must have a valid object for theme: $location")

                    states[stateName] = if (isNinepatch) parseNinePatchState(
                        location,
                        stateName,
                        stateObj,
                        defaultState as NinePatchThemeState
                    ) else parseSimpleState(
                        location,
                        stateName,
                        stateObj,
                        defaultState as SimpleThemeState
                    )
                }

                val variantsObj = el["variants"]?.asJsonObject
                val variants = mutableMapOf<String, StatefulTheme>()
                if (variantsObj is JsonObject) {
                    for ((variantName, variantObj) in variantsObj.entrySet()) {
                        if (variantObj !is JsonObject) throw IllegalStateException("Variant \"$variantName\" must have a valid object for theme: $location")

                        val variantStates = mutableMapOf<String, ThemeState>()
                        for ((stateName, stateObj) in variantObj.entrySet()) {
                            if (stateObj !is JsonObject) throw IllegalStateException("State \"$stateName\" of variant \"$variantName\" must have a valid object for theme: $location")

                            variantStates[stateName] = if (isNinepatch) parseNinePatchState(
                                location,
                                stateName,
                                statesObj,
                                defaultState as NinePatchThemeState
                            ) else parseSimpleState(
                                location,
                                stateName,
                                stateObj,
                                defaultState as SimpleThemeState
                            )
                        }

                        variants[variantName] = StatefulTheme(variantStates)
                    }
                }

                COMPOSABLES[location] = ComposableTheme(isNinepatch, states, variants)
                KLib.LOGGER.info(
                    "Theme \"{}\" has been registered with {} states and {} variants.{}",
                    location,
                    states.size,
                    variants.size,
                    if (isNinepatch) " It is also registered as a nine-patch" else ""
                )
            } catch (e: Exception) {
                KLib.LOGGER.warn("Error processing theme at $location: ${e.message}", e)
            }
        }
    }

    private fun parseThemeState(
        loc: ResourceLocation,
        stateName: String,
        el: JsonObject,
        default: ThemeState? = null
    ): Map<String, Any> = mapOf(
        "texture" to (default?.texture
            ?: el["texture"]?.asString?.let { ResourceLocation.parse(it) }
            ?: throw IllegalStateException("Texture path is invalid for simple state \"$stateName\" in: $loc")),
        "textureSize" to (el.parseSize("texture_size")
            ?: default?.textureSize
            ?: throw IllegalStateException("Texture size is invalid for simple state \"$stateName\" in: $loc")),
        "u" to (el["u"]?.asInt ?: default?.u ?: 0),
        "v" to (el["v"]?.asInt ?: default?.v ?: 0)
    )

    private fun parseNinePatchState(
        loc: ResourceLocation,
        stateName: String,
        el: JsonObject,
        default: NinePatchThemeState? = null
    ): ThemeState {
        val base = parseThemeState(loc, stateName, el, default)
        val repeat = (el["repeat"]?.asBoolean ?: default?.repeat) == true

        val patchSize = el.parseSize("patch_size")
        val cornersSize = el.parseSize("corners_size")
            ?: default?.cornersSize
            ?: patchSize
            ?: throw IllegalStateException("Corners patch size is invalid for nine-patch state \"$stateName\" in: $loc")
        val centerSize = el.parseSize("center_size")
            ?: default?.centerSize
            ?: patchSize
            ?: throw IllegalStateException("Center patch size is invalid for nine-patch state \"$stateName\" in: $loc")

        return NinePatchThemeState(
            base["texture"] as ResourceLocation,
            base["textureSize"] as Size,
            base["u"] as Int,
            base["v"] as Int,
            repeat,
            cornersSize,
            centerSize
        )
    }

    private fun parseSimpleState(
        loc: ResourceLocation,
        stateName: String,
        el: JsonObject,
        default: SimpleThemeState? = null
    ): ThemeState {
        val base = parseThemeState(loc, stateName, el, default)

        val width: Int = el["width"]?.asInt
            ?: default?.width
            ?: throw IllegalStateException("Width is invalid for simple state \"$stateName\" in: $loc")
        val height: Int = el["height"]?.asInt
            ?: default?.height
            ?: throw IllegalStateException("Height is invalid for simple state \"$stateName\" in: $loc")
        val uWidth: Int = el["uWidth"]?.asInt
            ?: default?.uWidth
            ?: width
        val vHeight: Int = el["vHeight"]?.asInt
            ?: default?.vHeight
            ?: height

        return SimpleThemeState(
            base["texture"] as ResourceLocation,
            base["textureSize"] as Size,
            base["u"] as Int,
            base["v"] as Int,
            width,
            height,
            uWidth,
            vHeight
        )
    }

    private fun JsonObject.parseSize(key: String): Size? {
        val sizeObj = this[key]?.asJsonObject ?: return null
        val w = sizeObj["width"]?.asInt ?: return null
        val h = sizeObj["height"]?.asInt ?: return null
        return Size(w, h)
    }
}