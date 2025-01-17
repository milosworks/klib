package xyz.milosworks.klib.test

import com.mojang.logging.LogUtils
import dev.architectury.platform.Mod
import dev.architectury.platform.Platform
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import xyz.milosworks.klib.KLib

/** @suppress supress for dokka */
object KTest {
	const val ID = "${KLib.ID}_test"

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