package xyz.milosworks.testmod

import net.minecraft.resources.ResourceLocation
import net.neoforged.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import xyz.milosworks.testmod.init.TBlocks
import xyz.milosworks.testmod.init.TCreativeTabs

@Mod(TestMod.ID)
object TestMod {
	const val ID = "testmod"
	val LOGGER: Logger = LogManager.getLogger(ID)

	init {
		TBlocks.init(MOD_BUS)
		TCreativeTabs.init(MOD_BUS)

//		PNChannels.init()
	}

	fun id(path: String): ResourceLocation {
		return ResourceLocation.fromNamespaceAndPath(ID, path)
	}
}