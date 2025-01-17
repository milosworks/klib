package xyz.milosworks.klib.test.init

import dev.architectury.registry.menu.MenuRegistry
import dev.architectury.registry.registries.DeferredRegister
import net.minecraft.core.registries.Registries
import net.minecraft.world.inventory.MenuType
import xyz.milosworks.klib.registry.RegistryHelper
import xyz.milosworks.klib.test.KTest
import xyz.milosworks.klib.test.client.ui.UIMenu
import xyz.milosworks.klib.test.client.ui.UIScreen

object TMenus : RegistryHelper<MenuType<*>>(DeferredRegister.create(KTest.ID,  Registries.MENU)) {
	val UI = register("ui") { ->
		MenuRegistry.ofExtended { id, inventory, buf -> UIMenu.getProvider(id, inventory.player) }
	}

	fun initClient() {
		MenuRegistry.registerScreenFactory(UI.get(), ::UIScreen)
	}
}