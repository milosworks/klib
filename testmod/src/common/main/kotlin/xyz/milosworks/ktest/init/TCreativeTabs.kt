package xyz.milosworks.ktest.init

import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import xyz.milosworks.klib.registry.CreativeTabRegistryHelper
import xyz.milosworks.ktest.KTest

object TCreativeTabs : CreativeTabRegistryHelper<CreativeModeTab>(KTest.ID) {
    val TAB by create("tab") { ->
        title(Component.literal("Test"))
        icon { ItemStack(TBlocks.TEST) }
        displayItems { _, o ->
            o.accept(TBlocks.TEST)
        }
    }
}
