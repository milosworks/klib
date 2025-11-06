package xyz.milosworks.ktest.init

import dev.architectury.registry.menu.MenuRegistry
import dev.architectury.registry.registries.DeferredRegister
import net.minecraft.core.registries.Registries
import net.minecraft.world.inventory.MenuType
import xyz.milosworks.klib.registry.RegistryHelper
import xyz.milosworks.ktest.KTest
import xyz.milosworks.ktest.ui.UIMenu
import xyz.milosworks.ktest.ui.UIScreen

object TMenus : RegistryHelper<MenuType<*>>(DeferredRegister.create(KTest.ID, Registries.MENU)) {
    @Suppress("DEPRECATION", "removal")
    val UI = register("ui") { ->
        MenuRegistry.of(::UIMenu)
    }

    fun initClient() {
        MenuRegistry.registerScreenFactory(UI.get(), ::UIScreen)
    }
}