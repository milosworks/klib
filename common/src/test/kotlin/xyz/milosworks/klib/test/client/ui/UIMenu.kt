package xyz.milosworks.klib.test.client.ui

import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import xyz.milosworks.klib.test.init.TMenus

class UIMenu(containerId: Int, val player: Player) :
	AbstractContainerMenu(TMenus.UI.get(), containerId) {
	companion object {
		fun getProvider(id: Int, player: Player): UIMenu =
			UIMenu(id, player)
	}

	override fun quickMoveStack(player: Player, index: Int): ItemStack = ItemStack.EMPTY
	override fun stillValid(player: Player): Boolean = true
}