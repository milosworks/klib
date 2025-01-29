package xyz.milosworks.klib.ui.util

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.PreparableReloadListener
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import xyz.milosworks.klib.KLib
import xyz.milosworks.klib.ui.layout.Size

data class NinePatchTexture(
	val texture: ResourceLocation,
	val textureSize: Size,
	val u: Int,
	val v: Int,
	val cornersSize: Size,
	val centerSize: Size,
	val repeat: Boolean
) {
	constructor(
		texture: ResourceLocation,
		textureSize: Size,
		u: Int,
		v: Int,
		patchSize: Size,
		repeat: Boolean
	) : this(texture, textureSize, u, v, patchSize, patchSize, repeat)

	companion object {
		fun of(texture: ResourceLocation): NinePatchTexture? =
			NPTResourceLoader.TEXTURES[texture]
	}
}

// REMEMBER: SimpleJsonResourceReloadListener expects a type in 1.21.4 in this case being NinePatchTexture

class NPTResourceLoader : SimpleJsonResourceReloadListener(Gson(), "nine_patch_textures"), PreparableReloadListener {
	companion object {
		val TEXTURES = mutableMapOf<ResourceLocation, NinePatchTexture>()
	}

	override fun apply(
		objs: Map<ResourceLocation, JsonElement>,
		resourceManager: ResourceManager,
		profiler: ProfilerFiller
	) {
		try {
			objs.forEach { (loc, el) ->
				if (el !is JsonObject) return@forEach

				val texture = ResourceLocation.parse(el["texture"].asString)
				val textureSize = el.selfSize("texture")
					?: throw IllegalStateException("Texture size is invalid for nine patch texture: $loc")
				val u = el["u"]?.asInt ?: 0
				val v = el["v"]?.asInt ?: 0
				val repeat = el["repeat"]?.asBoolean == true

				if (el["corners_size"] != null) {
					val cornersSize = el.parseSize("corners_size")
						?: throw IllegalStateException("Corners patch size is invalid for nine patch texture: $loc")
					val centerSize = el.parseSize("center_size")
						?: throw IllegalStateException("Center patch size is invalid for nine patch texture: $loc")

					TEXTURES[loc] = NinePatchTexture(texture, textureSize, u, v, cornersSize, centerSize, repeat)
				} else {
					val patchSize = el.parseSize("patch_size")
						?: throw IllegalStateException("Patch size is invalid for nine patch texture: $loc")

					TEXTURES[loc] = NinePatchTexture(texture, textureSize, u, v, patchSize, repeat)
				}
			}
		} catch (e: Exception) {
			KLib.LOGGER.warn(e.toString())
		}
	}

	private fun JsonObject.selfSize(key: String): Size? {
		val w = this["${key}_width"]?.asInt ?: return null
		val h = this["${key}_height"]?.asInt ?: return null
		return Size(w, h)
	}

	private fun JsonObject.parseSize(key: String): Size? {
		val sizeObj = this[key]?.asJsonObject ?: return null
		val w = sizeObj["width"]?.asInt ?: return null
		val h = sizeObj["height"]?.asInt ?: return null
		return Size(w, h)
	}
}