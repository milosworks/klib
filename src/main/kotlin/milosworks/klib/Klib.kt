package milosworks.klib

import net.minecraft.resources.ResourceLocation
import net.neoforged.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.spongepowered.asm.mixin.MixinEnvironment

@Mod(Klib.ID)
object Klib {
	const val ID = "klib"
	val LOGGER: Logger = LogManager.getLogger(ID)

	init {
		MixinEnvironment.getCurrentEnvironment().audit()
	}

	fun id(path: String): ResourceLocation {
		return ResourceLocation.fromNamespaceAndPath(ID, path)
	}
}