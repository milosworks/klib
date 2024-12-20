package xyz.milosworks.testmod.init

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.registries.DeferredRegister
import xyz.milosworks.klib.registry.RegistryHelper
import xyz.milosworks.testmod.TestMod

object TCreativeTabs :
	RegistryHelper<CreativeModeTab>(DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, TestMod.ID)) {
	val TAB by register("tab") { ->
		CreativeModeTab.builder()
			.title(Component.literal("Test"))
			.icon { ItemStack(TBlocks.TEST) }
			.displayItems { _, o ->
				o.accept(TBlocks.TEST)
			}
			.build()
	}
}
