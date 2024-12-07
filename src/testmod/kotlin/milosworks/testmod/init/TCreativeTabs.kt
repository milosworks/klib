package milosworks.testmod.init

import milosworks.testmod.TestMod
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredRegister
import thedarkcolour.kotlinforforge.neoforge.forge.getValue

object TCreativeTabs {
	private val CREATIVE_TABS: DeferredRegister<CreativeModeTab> = DeferredRegister.create(
		BuiltInRegistries.CREATIVE_MODE_TAB,
		TestMod.ID
	)

	val TAB by CREATIVE_TABS.register("tab") { ->
		CreativeModeTab.builder()
			.title(Component.literal("Test"))
			.icon { ItemStack(TBlocks.TEST) }
			.displayItems { _, o ->
				o.accept(TBlocks.TEST)
			}
			.build()
	}

	fun init(event: IEventBus) {
		CREATIVE_TABS.register(event)
	}
}
