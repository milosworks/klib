package xyz.milosworks.testmod.init

import TestMod
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import xyz.milosworks.klib.registry.CreativeTabRegistryHelper
import xyz.milosworks.testmod.init.TCreativeTabs.getValue

object TCreativeTabs : CreativeTabRegistryHelper<CreativeModeTab>(TestMod.ID) {
	val TAB by create("tab") { ->
		title(Component.literal("Test"))
		icon { ItemStack(TBlocks.TEST) }
		displayItems { _, o ->
			o.accept(TBlocks.TEST)
		}
	}
}
