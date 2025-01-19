package xyz.milosworks.klib.test

import com.mojang.logging.LogUtils
import dev.architectury.event.events.client.ClientLifecycleEvent
import dev.architectury.platform.Mod
import dev.architectury.platform.Platform
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import xyz.milosworks.klib.KLib
import xyz.milosworks.klib.test.init.TBlocks
import xyz.milosworks.klib.test.init.TChannels
import xyz.milosworks.klib.test.init.TCreativeTabs
import xyz.milosworks.klib.test.init.TMenus

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
		TBlocks.init()
		TChannels.init()
		TCreativeTabs.init()
		TMenus.init()
	}

	@JvmStatic
	fun initClient() {
		ClientLifecycleEvent.CLIENT_SETUP.register {
			TMenus.initClient()
		}
	}

	@JvmStatic
	fun initCommon() {
	}
}