package xyz.milosworks.klib.ui.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import xyz.milosworks.klib.serialization.serializers.SResourceLocation
import xyz.milosworks.klib.ui.components.TextureStates
import xyz.milosworks.klib.ui.layout.Size

@Serializable
sealed interface KThemeState {
    val texture: SResourceLocation?

    val textureSize: Size?
    val u: Int
    val v: Int
}

interface INinePatchThemeState : KThemeState {
    val repeat: Boolean
    val cornersSize: Size
    val centerSize: Size
}

@Serializable
data class KNinePatchThemeState(
    override val texture: SResourceLocation?,
    @SerialName("texture_size")
    override val textureSize: Size?,
    override val u: Int = 0,
    override val v: Int = 0,
    val repeat: Boolean = false,
    @SerialName("corners_size")
    val cornersSize: Size?,
    @SerialName("center_size")
    val centerSize: Size?,
) : KThemeState

interface ISimpleThemeState : KThemeState {
    val width: Int
    val height: Int
    val uWidth: Int
    val vHeight: Int
}

@Serializable
data class KSimpleThemeState(
    override val texture: SResourceLocation?,
    @SerialName("texture_size")
    override val textureSize: Size?,
    override val u: Int = 0,
    override val v: Int = 0,
    val width: Int?,
    val height: Int?,
    @SerialName("u_width")
    val uWidth: Int?,
    @SerialName("v_height")
    val vHeight: Int?,
) : KThemeState

@Serializable
data class KStatefulTheme(val states: Map<String, KThemeState>)

@Serializable
data class KComposableTheme(
    @SerialName("ninepatch")
    val isNinepatch: Boolean,
    val states: Map<String, KThemeState>,
    val variants: Map<String, KStatefulTheme> = emptyMap(),
) {
    companion object {
//        operator fun get(loc: ResourceLocation): KComposableTheme =
//            ThemeResourceListener.COMPOSABLES[loc] ?: throw IllegalStateException("No theme found for composable: $loc")
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun getState(stateName: String, variantName: String): KThemeState =
        variants[variantName]?.states[stateName] ?: states[stateName] ?: states[TextureStates.DEFAULT]!!

    @Suppress("NOTHING_TO_INLINE")
    inline fun hasState(stateName: String, variantName: String?): Boolean =
        (variantName?.let { variants[it] }?.states[stateName] ?: states[stateName]) != null
}

object ComposableThemeSerializer : KSerializer<KComposableTheme> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ComposableTheme") {
        element<Boolean>("isNinepatch")
        element("states", MapSerializer(String.serializer(), KThemeState.serializer()).descriptor)
        element(
            "variants",
            MapSerializer(String.serializer(), KStatefulTheme.serializer()).descriptor,
            isOptional = true
        )
    }

    override fun deserialize(decoder: Decoder): KComposableTheme {
        val jsonDecoder = decoder as? JsonDecoder ?: error("This decoder only supports JSON")
        val json = decoder.decodeJsonElement().jsonObject

        val isNinepatch = json["isNinepatch"]?.jsonPrimitive?.booleanOrNull ?: true

        val statesObj =
            json["states"]?.jsonObject ?: throw SerializationException("Theme must have a valid states object")
        val defaultObj = statesObj["default"]?.jsonObject
            ?: throw SerializationException("Theme must have a valid \"default\" state object")

        val defaultState =
            if (isNinepatch) jsonDecoder.json.decodeFromJsonElement(KNinePatchThemeState.serializer(), defaultObj)
            else jsonDecoder.json.decodeFromJsonElement(KSimpleThemeState.serializer(), defaultObj)

        val states = statesObj.mapValues { (name, el) ->
            if (name == "default") defaultState
            else {
                val merged = defaultObj.mergeWith(
                    el as? JsonObject ?: throw SerializationException("State \"$name\" must have a valid object")
                )

                if (isNinepatch) jsonDecoder.json.decodeFromJsonElement(KNinePatchThemeState.serializer(), merged)
                else jsonDecoder.json.decodeFromJsonElement(KSimpleThemeState.serializer(), merged)
            }
        }

        val variants = json["variants"]?.jsonObject?.mapValues { (variantName, variant) ->
            KStatefulTheme(variant.jsonObject.mapValues { (name, el) ->
                val merged = defaultObj.mergeWith(
                    el as? JsonObject
                        ?: throw SerializationException("State \"$name\" of variant \"$variantName\" must have a valid object")
                )

                if (isNinepatch) jsonDecoder.json.decodeFromJsonElement(KNinePatchThemeState.serializer(), merged)
                else jsonDecoder.json.decodeFromJsonElement(KSimpleThemeState.serializer(), merged)
            })
        } ?: emptyMap()

        return KComposableTheme(isNinepatch, states, variants)
    }

    private fun JsonObject.mergeWith(other: JsonObject): JsonObject =
        JsonObject(this.toMutableMap().apply { putAll(other) })

    override fun serialize(encoder: Encoder, value: KComposableTheme) {
        error("Serialization is not supported!")
    }
}