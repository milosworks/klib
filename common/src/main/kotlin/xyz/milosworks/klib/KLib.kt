package xyz.milosworks.klib

import com.mojang.logging.LogUtils
import dev.architectury.platform.Mod
import dev.architectury.platform.Platform
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger

/** @suppress suppress for dokka */
object KLib {
	const val ID = "klib"

	@JvmField
	val MOD: Mod = Platform.getMod(ID)

	@JvmField
	val LOGGER: Logger = LogUtils.getLogger()

	@JvmStatic
	operator fun get(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(ID, path)

	@JvmStatic
	fun init() {
	}

	@JvmStatic
	fun initClient() {
	}

	@JvmStatic
	fun initCommon() {
	}
}